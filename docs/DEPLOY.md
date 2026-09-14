# Deploy em produção — SISUMONI

Guia passo a passo para colocar o SISUMONI no ar numa VPS, com HTTPS via Let's Encrypt.

## 0. Antes de começar

- **VPS**: recomendado no mínimo **2 GB de RAM** (o orçamento de memória dos containers deste compose soma ~1,15 GB, sobrando espaço pro sistema operacional e o SSH). Com 1 GB fica muito apertado.
- **Domínio**: você precisa de um domínio (ou subdomínio) já registrado, com o DNS **apontando pro IP da VPS** (registro tipo `A`). Confirme antes de continuar:
  ```bash
  dig +short seu-dominio.com.br
  ```
  O IP retornado precisa ser o IP público da sua VPS.
- **Portas liberadas no firewall da VPS**: `22` (SSH), `80` (HTTP) e `443` (HTTPS).
- **Docker e Docker Compose instalados na VPS** (`docker --version` e `docker compose version`).

Em todos os arquivos deste projeto, o domínio aparece como `seu-dominio.com.br` — depois de clonar o repo na VPS, troque isso pelo domínio real em **dois arquivos**:
```bash
sed -i 's/seu-dominio.com.br/SEU-DOMINIO-REAL.com.br/g' nginx/default.conf.initial nginx/default.conf.ssl
```

## 1. Preparar os segredos

Na raiz do projeto, na VPS:
```bash
cp .env.production.example .env.production
```
Edite `.env.production` e preencha:
- `SPRING_DATASOURCE_PASSWORD` — uma senha forte pro Postgres.
- `JWT_SECRET` — gere com `openssl rand -base64 48`. **Não deixe o valor de desenvolvimento.**
- `ADMIN_EMAIL` / `ADMIN_SENHA` — credenciais do administrador principal. O `application.yaml` do backend tem `Admin@2026!` como padrão de desenvolvimento, **público no repositório** — você precisa sobrescrever isso aqui, senão o admin de produção fica com uma senha conhecida.
- `SPRING_MAIL_*` — credenciais do provedor de e-mail (recuperação de senha).
- `DOMAIN` e `CERTBOT_EMAIL` — o domínio real e um e-mail seu (o Let's Encrypt usa pra avisar sobre expiração de certificado).

Crie também um link simbólico `.env` apontando pro `.env.production`:
```bash
ln -s .env.production .env
```
Isso é necessário porque o `docker-compose.prod.yml` usa variáveis tipo `${SPRING_DATASOURCE_USERNAME}` diretamente no YAML (pra montar `POSTGRES_USER`/`POSTGRES_PASSWORD` e o healthcheck do Postgres) — essas substituições só são resolvidas pelo Compose CLI a partir de um arquivo chamado exatamente `.env`, não `.env.production`. Sem o link, essas variáveis ficam em branco (aparecem avisos como `The "SPRING_DATASOURCE_USERNAME" variable is not set`) e o container do Postgres sobe sem usuário/senha válidos, falhando no healthcheck (`dependency failed to start: container ... is unhealthy`).

## 2. Buildar as imagens

```bash
docker compose -f docker-compose.prod.yml build
```

## 3. Bootstrap — subir só com HTTP

O Nginx não consegue subir com HTTPS antes de existir um certificado, e o Certbot precisa do Nginx respondendo em HTTP pra conseguir emitir o certificado. Por isso, o primeiro boot é só HTTP.

No `docker-compose.prod.yml`, edite a linha do volume do serviço `nginx` pra apontar pro `.initial` (em vez do `.ssl`):
```yaml
    volumes:
      - ./nginx/default.conf.initial:/etc/nginx/conf.d/default.conf
```
Depois suba tudo (exceto o certbot de renovação contínua, que só faz sentido depois):
```bash
docker compose -f docker-compose.prod.yml up -d db backend frontend nginx
```

Acompanhe o primeiro boot do backend — as 9 migrations do Flyway (V1 a V9) precisam aplicar sem erro:
```bash
docker compose -f docker-compose.prod.yml logs -f backend
```
(Ctrl+C quando ver `Started BackendApplication`.)

## 4. Emitir o certificado

Primeiro, um certificado de **teste** (staging), pra validar o fluxo sem gastar a cota de emissão real do Let's Encrypt:
```bash
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot -w /var/www/certbot \
  -d SEU-DOMINIO-REAL.com.br \
  --email seu-email@exemplo.com \
  --agree-tos --no-eff-email --staging
```
Se der certo (sem erros de validação), apague o certificado de teste e peça o de verdade:
```bash
docker compose -f docker-compose.prod.yml run --rm certbot delete --cert-name SEU-DOMINIO-REAL.com.br
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot -w /var/www/certbot \
  -d SEU-DOMINIO-REAL.com.br \
  --email seu-email@exemplo.com \
  --agree-tos --no-eff-email
```

## 5. Cortar para HTTPS

Volte a linha do volume do `nginx` em `docker-compose.prod.yml` pra apontar pro `.ssl`:
```yaml
    volumes:
      - ./nginx/default.conf.ssl:/etc/nginx/conf.d/default.conf
```
E reinicie:
```bash
docker compose -f docker-compose.prod.yml up -d nginx
```

## 6. Ligar a renovação automática

```bash
docker compose -f docker-compose.prod.yml up -d certbot
```
Esse serviço fica rodando em loop, tentando renovar a cada 12h (o Certbot só renova de verdade quando o certificado está perto de expirar).

**Importante**: renovar o certificado não recarrega o Nginx sozinho. Adicione um cron no host (`crontab -e`) pra recarregar periodicamente:
```
0 3 * * * cd /caminho/do/projeto && docker compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

Teste que a renovação automática vai funcionar sem esperar os ~90 dias reais:
```bash
docker compose -f docker-compose.prod.yml run --rm certbot renew --dry-run
```

## 7. Verificação final

- `curl -I https://SEU-DOMINIO-REAL.com.br` — deve responder `200` com certificado válido.
- `curl -I http://SEU-DOMINIO-REAL.com.br` — deve responder `301` redirecionando pra HTTPS.
- Abra `https://SEU-DOMINIO-REAL.com.br` no navegador e teste o login.
- `curl https://SEU-DOMINIO-REAL.com.br/actuator/health` — deve responder `{"status":"UP"}`.

## Atualizando o deploy depois

```bash
git pull
docker compose -f docker-compose.prod.yml build
docker compose -f docker-compose.prod.yml up -d
```
