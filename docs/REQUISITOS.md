## SISUMONI

## Contexto do projeto

As pessoas que compõem o K0 realizam a classificação dos estudantes nas vagas de monitoria, todo início de período(durante 1 semana), modificando o dado de cada estudante(média de 130 por período) e suas alterações nas classificações manualmente. Gastando muito tempo e esforço para chegar no resultado final.

Esse Sistema serve para organizar os Estudantes que vão se candidatar para o processo no Edital da Universidade. O Sistema apenas informa em quais posições os Estudantes vão ficar, os procedimentos de alunos em fila de espera, desistência vão ser resolvidos pela Universidade.

## Requisitos Funcionais:

| ID | Descrição |   |   |   |   | Prioridade |
| --- | --- | --- | --- | --- | --- | --- |
| RF001 | Administradores | e | Operadores | devem | poder | Alta |
|   | cadastrar/deletar/editar Estudantes com nome, matrícula, |   |   |   |   |   |
|   | turma, nome fantasia, IRA, 1ª opção, média da 1ª opção, 2ª |   |   |   |   |   |
|   | opção e média da 2ª opção. Os Operadores só podem |   |   |   |   |   |
|   | cadastrar/deletar/editar Estudantes da(s) turma(s) pelas |   |   |   |   |   |
|   | quais são responsáveis. |   |   |   |   |   |
| RF002 | Somente os | Administradores |   | devem | poder | Alta |
|   | cadastrar/deletar as Turmas. Deletar apenas Turmas sem |   |   |   |   |   |
|   | Estudantes vinculados |   |   |   |   |   |
| RF003 | Somente os | Administradores |   | devem | poder | Alta |
|   | cadastrar/deletar | os |   | Departamentos. | Deletar |   |
|   | Departamentos que não tenham Vagas de Monitoria |   |   |   |   |   |
|   | Vinculadas |   |   |   |   |   |
| RF004 | Somente os | Administradores |   | devem | poder | Alta |
|   | cadastrar/deletar as Vagas de Monitoria com nome da |   |   |   |   |   |
|   | disciplina, Departamento vinculado , turmas que podem se |   |   |   |   |   |
|   | candidatar à essa vaga, quantidade de vagas bolsistas, |   |   |   |   |   |
|   | quantidade de vagas voluntárias, quantidade de lista de |   |   |   |   |   |


|   | espera, e o professor da disciplina.Deletar apenas não |   |   |   |   |   |
| --- | --- | --- | --- | --- | --- | --- |
|   | tenham Estudantes se candidatando para a Vaga |   |   |   |   |   |
| RF005 | Somente | os | Administradores | devem | poder | Alta |
|   | cadastrar/deletar os Operadores com nome, email, senha e |   |   |   |   |   |
|   | turmas das quais são responsáveis. Sem restrição ou efeito |   |   |   |   |   |
|   | quando deletar um Operador. |   |   |   |   |   |
| RF006 | O Sistema deve permitir que Administradores e Operadores |   |   |   |   | Alta |
|   | façam login com e-mail e senha. |   |   |   |   |   |
| RF007 | A conta do Administrador principal deve ser criada na |   |   |   |   | Alta |
|   | configuração inicial do sistema |   |   |   |   |   |
| RF008 | Em caso de perda de senha o Sistema deve enviar um |   |   |   |   | Baixa |
|   | email de recuperação. |   |   |   |   |   |
| RF009 | O Sistema deve calcular as classificações sempre que |   |   |   |   | Alta |
|   | houver uma mudança nos Estudantes.(calculando a |   |   |   |   |   |
|   | pontuação pela soma IRA + Média da Opção) |   |   |   |   |   |
| RF010 | O Sistema deve poder exportar um relatório .pdf com a |   |   |   |   | Baixa |
|   | classificação apenas com os nomes fantasia dos |   |   |   |   |   |
|   | Estudantes(Administradores e Operadores) e outro relatório |   |   |   |   |   |
|   | com a classificação com os nomes reais dos |   |   |   |   |   |
|   | Estudantes(Apenas Administradores). |   |   |   |   |   |
| RF011 | Os relatórios devem ter as Vagas com divididas por |   |   |   |   | Baixa |
|   | Departamento mostrando a posição dos Estudantes |   |   |   |   |   |
|   | classificados, mostrando caso tenham bolsas e a Lista de |   |   |   |   |   |
|   | Espera. Mostrar apenas posição, nome/ nome fantasia, tipo |   |   |   |   |   |
|   | de vaga. |   |   |   |   |   |
| RF012 | Em caso de empate deve gerar um aviso para os |   |   |   |   | Baixa |
|   | Operadores das Turmas dos Estudantes empatados para |   |   |   |   |   |
|   | decidirem quem vai ficar com a vaga, decidido um dos |   |   |   |   |   |
|   | Operadores informa ao Sistema por meio de um botão que |   |   |   |   |   |
|   | irá aparecer junto com o aviso, quem vai estar na frente e o |   |   |   |   |   |
|   | Sistema vai |   |   |   | calcular a classificação novamente |   |
|   | considerando quem está na frente entre os Estudantes. |   |   |   |   |   |
| RF013 | O Administrador vai poder visualizar todos os Estudantes e |   |   |   |   | Alta |
|   | todas as Classificações |   |   |   |   |   |
| RF014 | Os Operadores só podem visualizar os Estudantes de suas |   |   |   |   | Alta |
|   | Turmas. |   |   |   |   |   |


| RF015 | Os Operadores podem ver todas as Vagas e as | Média |
| --- | --- | --- |
|   | classificações de outros Estudantes, mesmo não |   |
|   | pertencendo às suas Turmas |   |

## Stakeholders identificados:

| Gabriel Fontes Presidente do K0 / Administrador | Alta influência |
| --- | --- |
| Principal |   |
| Administrador Pessoa de confiança do K0 |   |
| responsável por monitorar o processo |   |
| Operador Pessoas que participam do K0 | Usuário Principal |
| responsáveis pelo cadastro dos |   |
| Estudantes |   |
| Estudantes Discentes da Universidade que se | Usuário Externo |
| candidatam às vagas |   |

## Requisitos não funcionais:

| ID | Descrição | Categoria |
| --- | --- | --- |
| RNF001 | O Sistema deve atualizar a classificação do | Desempenho |
|   | Estudantes em menos de 2 segundos |   |
| RNF002 | Apenas os Administradores ou os Operadores da | Segurança |
|   | Turma devem ter acesso aos dados dos Estudantes |   |
|   | de uma Turma |   |
| RNF003 | O Sistema deve estar 99% disponível durante o | Disponibilidade |
|   | período do processo |   |

## Glossário do Domínio:

| Termo | Definição no contexto do Sistema |
| --- | --- |
| IRA | Índice de Rendimento do Aluno -> Informação calculada pela |
|   | Universidade entregue ao Estudante e fornecido aos Operadores |
|   | pelo WhatsApp |
| K0 | Centro Acadêmico de Medicina |
| Nome | Nome escolhido pelos Estudantes para ao fornecer a |
| Fantasia | Classificação Parcial mantenha o sigilo dos Estudantes |


| Administrador | Pessoa de confiança do K0 responsável por monitorar o processo |
| --- | --- |
| Vaga Bolsista | Algumas Vagas podem fornecer bolsas para os Estudantes |
|   | classificados de acordo com o Edital da Universidade. |
| Operador | Pessoas que participam do K0 responsáveis pelo cadastro dos |
|   | Estudantes |
| Vaga | Vagas que não possuem bolsas para os Estudantes. |
| Voluntária |   |
| Lista de | Caso aconteça a desistência de algum Estudante quem vai |
| Espera | assumir vai ser a pessoa alocada na Lista de Espera. |

## Restrições do Projeto:

Prazo de entrega: Primeira semana de Agosto de 2026.

Orçamento: Baixo custo -> Utilizar uma VPS de baixo custo usando conteinerização via Docker.

Comunicação Estudante e Operador: Vai se manter pelo WhatsApp.

## Controle de Acesso:

| Ação | Administrador | Operador |
| --- | --- | --- |
| Autenticação |   |   |
| Fazer login com e-mail e senha(RF006) | Sim | Sim |
| Recuperar senha por e-mail(RF008) | Sim | Sim |
| Gestão de Usuários |   |   |
| Criar conta de Administrador | Só na config. inicial | Não |
| principal(RF007) |   |   |
| Cadastrar / deletar Operadores(RF005) | Sim | Não |
| Desativar conta de Operador(RF005) | Sim | Não |
| Estudantes |   |   |
| Cadastrar Estudante(RF001) | Sim | Só da sua |
|   |   | Turma |
| Editar Estudante(RF001) | Sim | Só da sua |
|   |   | Turma |


| Deletar Estudante(RF001) | Sim | Só da sua |
| --- | --- | --- |
|   |   | Turma |
| Visualizar todos os Estudantes(RF013) | Sim | Não |
| Visualizar Estudantes da sua | Sim | Sim |
| Turma(RF014) |   |   |
| Dados Cadastrais |   |   |
| Cadastrar / deletar Turmas(RF002) | Sim | Não |
| Cadastrar / deletar | Sim | Não |
| Departamentos(RF003) |   |   |
| Cadastrar / deletar Vagas de | Sim | Não |
| Monitoria(RF004) |   |   |
| Classificação |   |   |
| Visualizar todas as | Sim | Somente |
| Classificações(RF013) |   | Vagas(RF015) |
| Ação |   |   |
| Ver todas as Vagas e | Sim | Sim |
| Classificações(RF015) |   |   |
| Resolver empate(RF012) | Não | Operadores |
|   |   | das turmas |
|   |   | envolvidas |
| Relatórios |   |   |
| Gerar relatório com nomes | Sim | Sim |
| fantasia(RF010) |   |   |
| Gerar relatório com nomes reais(RF010) | Sim | Não |

## Critérios de Aceitação

RF001 [Alta] Cadastrar / Editar / Deletar Estudantes

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Preencho todos os | O Estudante aparece na |
|   | logado | campos obrigatórios e | lista da turma |
|   |   | salvo | selecionada |


| Sucesso | Sou Operador da | Cadastro um | O cadastro é realizado |
| --- | --- | --- | --- |
|   | Turma A logado | Estudante na Turma | com sucesso |
|   |   | A |   |
| Restrição | Sou Operador da | Tento cadastrar um | O sistema bloqueia e |
|   | Turma A | Estudante na Turma | exibe mensagem de |
|   |   | B | erro |
| Validação | Estou cadastrando | Deixo um campo | O sistema indica o |
|   | um Estudante | obrigatório vazio e | campo faltante e não |
|   |   | salvo | salva |
| Borda | Cadastro um | Salvo o formulário | O sistema impede |
|   | Estudante com |   | duplicata e informa o |
|   | matrícula já existente |   | conflito |
| Edição | Sou Administrador e | Salvo a alteração | O Estudante é |
|   | edito o IRA de um |   | atualizado e a |
|   | Estudante |   | classificação é |
|   |   |   | recalculada |
|   |   |   | automaticamente |
| Edição | Sou Operador da | Edito um Estudante | A edição é salva com |
|   | Turma A | da Turma A | sucesso |
| Restrição | Sou Operador da | Tento editar um | O sistema bloqueia e |
|   | Turma A | Estudante da Turma | exibe mensagem de |
|   |   | B | erro |
| Deleção | Sou Administrador e | Confirmo a exclusão O Estudante é removido |   |
|   | deleto um Estudante |   | e a classificação é |
|   |   |   | recalculada |
| Restrição | Sou Operador da | Tento deletar um | O sistema bloqueia e |
|   | Turma A | Estudante da Turma | exibe mensagem de |
|   |   | B | erro |

*RF002 [Alta] Cadastrar / Deletar Turmas*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Cadastro uma Turma | A Turma aparece na |
|   | logado | com nome válido | lista de Turmas |
|   |   |   | disponíveis |
| Restrição | Sou Operador | Tento acessar o | O sistema nega o |
|   |   | cadastro de Turmas | acesso |


| Deleção | A Turma não possui | O Administrador a | A Turma é removida da |
| --- | --- | --- | --- |
|   | Estudantes | deleta | lista |
|   | vinculados |   |   |
| Bloqueio | A Turma possui | O Administrador tenta | O sistema bloqueia e |
|   | Estudantes | deletá-la | exibe mensagem |
|   | vinculados |   | informando os vínculos |
|   |   |   | existentes |

*RF003 [Alta] Cadastrar / Deletar Departamentos*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Cadastro um | O Departamento |
|   | logado | Departamento com | aparece disponível para |
|   |   | nome válido | vínculo com Vagas |
| Restrição | Sou Operador | Tento acessar o | O sistema nega o |
|   |   | cadastro de | acesso |
|   |   | Departamentos |   |
| Deleção | O Departamento não | O Administrador o | O Departamento é |
|   | possui Vagas | deleta | removido da lista |
|   | vinculadas |   |   |
| Bloqueio | O Departamento | O Administrador tenta | O sistema bloqueia e |
|   | possui Vagas | deletá-lo | exibe mensagem |
|   | vinculadas |   | informando os vínculos |
|   |   |   | existentes |

*RF004 [Alta] Cadastrar / Deletar Vagas de Monitoria*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Cadastro uma Vaga | A Vaga aparece |
|   | logado | com todos os campos | disponível para |
|   |   | obrigatórios | classificação das |
|   |   |   | Turmas vinculadas |
| Validação | Estou cadastrando | Deixo o | O sistema impede o |
|   | uma Vaga | Departamento | cadastro e indica o |
|   |   | vinculado em branco | campo obrigatório |
|   |   | e salvo |   |
| Deleção | A Vaga não possui | O Administrador a | A Vaga é removida da |
|   | Estudantes | deleta | lista |
|   | candidatos |   |   |


| Bloqueio | A Vaga possui | O Administrador tenta | O sistema bloqueia e |
| --- | --- | --- | --- |
|   | Estudantes | deletá-la | exibe mensagem |
|   | candidatos |   | informando as |
|   |   |   | candidaturas ativas |
| Restrição | Sou Operador | Tento acessar o | O sistema nega o |
|   |   | cadastro de Vagas | acesso |

## RF005 [Alta] Cadastrar / Deletar Operadores

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Cadastro um | O Operador consegue |
|   | logado | Operador com nome, | fazer login com as |
|   |   | e-mail, senha e | credenciais cadastradas |
|   |   | turmas |   |
| Restrição | Sou Operador | Tento acessar o | O sistema nega o |
|   |   | cadastro de | acesso |
|   |   | Operadores |   |
| Validação | Cadastro um | Salvo o formulário | O sistema impede |
|   | Operador com e-mail |   | duplicata e exibe |
|   | já existente |   | mensagem de conflito |
| Deleção | Um Operador é | Ele tenta fazer login O sistema nega o |   |
|   | deletado |   | acesso com mensagem |
|   |   |   | de conta inativa |

## RF006 [Alta] Login com e-mail e senha

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Insiro e-mail e senha | Sou autenticado e |
|   | com conta ativa | corretos | redirecionado para a |
|   |   |   | tela inicial com visão de |
|   |   |   | todos os dados |
| Sucesso | Sou Operador com | Insiro e-mail e senha | Sou autenticado e vejo |
|   | conta ativa | corretos | apenas as turmas das |
|   |   |   | quais sou responsável |
| Falha | Insiro senha incorreta Tento fazer login |   | O sistema exibe |
|   |   |   | mensagem de erro sem |
|   |   |   | revelar se o e-mail |
|   |   |   | existe ou não |


| Falha | Minha conta foi | Tento fazer login | O sistema nega o |
| --- | --- | --- | --- |
|   | desativada pelo |   | acesso com mensagem |
|   | Administrador |   | de conta inativa |

*RF007 [Alta] Conta do Administrador principal criada na configuração inicial*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | O sistema é iniciado | A configuração inicial | Uma conta de |
|   | pela primeira vez | é executada | Administrador principal |
|   |   |   | é criada e funcional |
|   |   |   | para login |
| Borda | O sistema já foi | A configuração inicial | O sistema não cria um |
|   | configurado | é executada | segundo Administrador |
|   | anteriormente | novamente | principal nem |
|   |   |   | sobrescreve o existente |

*RF008 [Baixa] Recuperação de senha por e-mail*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Insiro um e-mail | Solicito recuperação | Recebo um e-mail com |
|   | cadastrado no | de senha | link de redefinição em |
|   | sistema |   | até 2 minutos |
| Borda | Insiro um e-mail não | Solicito recuperação | O sistema exibe |
|   | cadastrado | de senha | mensagem genérica |
|   |   |   | sem confirmar se o |
|   |   |   | e-mail existe |
| Expiração | Recebi o link de | Tento usá-lo após 24 | O sistema informa que |
|   | recuperação | horas | o link expirou e sugere |
|   |   |   | nova solicitação |

RF009 [Alta] Cálculo de classificação por soma IRA + Média da Opção, decrescente *Fórmula: Pontuação = IRA + Média da Opção escolhida (ex: 8,895 + 9,5 = 18,395). Os Estudantes são ordenados pela pontuação em ordem decrescente. Empate ocorre somente quando a soma é idêntica para dois ou mais Estudantes.*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Ana tem IRA 8,895 e | A classificação é | Ana aparece em 1º |
|   | Média 9,5 (pontuação | calculada para a Vaga | lugar e Bruno em 2º |
|   | 18,395). Bruno tem |   | lugar |
|   | IRA 8,0 e Média 9,0 |   |   |
|   | (pontuação 17,0) |   |   |


| Sucesso | Carlos tem IRA 7,0 e | A classificação é | Carlos aparece à frente |
| --- | --- | --- | --- |
|   | Média 9,5 (pontuação | calculada | de Diana — a |
|   | 16,5). Diana tem IRA |   | pontuação total é o |
|   | 9,0 e Média 7,0 |   | único critério, não o IRA |
|   | (pontuação 16,0) |   | isolado |
| Empate | Dois Estudantes têm | A classificação é | O sistema gera aviso de |
|   | IRA + Média com | calculada | empate ao Operador |
|   | resultado idêntico na |   | para decidir a posição |
|   | mesma Vaga (ex: 8,0 |   | (RF012) |
|   | + 8,0 = 16,0 e 9,0 + |   |   |
|   | 7,0 = 16,0) |   |   |
| Gatilho | O IRA de um | A alteração é salva | A pontuação é |
|   | Estudante é |   | recalculada (IRA + |
|   | atualizado |   | Média) e a classificação |
|   |   |   | é atualizada |
|   |   |   | automaticamente em |
|   |   |   | menos de 2 segundos |
| Gatilho | A Média da Opção de | A alteração é salva | A pontuação é |
|   | um Estudante é |   | recalculada (IRA + |
|   | atualizada |   | Média) e a classificação |
|   |   |   | é atualizada |
|   |   |   | automaticamente em |
|   |   |   | menos de 2 segundos |
| Gatilho | Um novo Estudante é | O cadastro é salvo | A classificação é |
|   | cadastrado |   | recalculada incluindo a |
|   |   |   | pontuação do novo |
|   |   |   | Estudante |
| Borda | Um Estudante é | O sistema recalcula | As posições dos demais |
|   | deletado |   | Estudantes são |
|   |   |   | atualizadas |
|   |   |   | corretamente com base |
|   |   |   | nas pontuações |
|   |   |   | restantes |

*RF010 [Baixa] Exportar relatório PDF com nome fantasia e nome real*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Operador logado Exporto o relatório |   | O PDF contém apenas |
|   |   |   | nomes fantasia, sem |
|   |   |   | qualquer nome real |
|   |   |   | visível |


| Sucesso | Sou Administrador | Exporto o relatório | O PDF mostra nome |
| --- | --- | --- | --- |
|   | logado | com nomes reais | real, posição e tipo de |
|   |   |   | vaga organizados por |
|   |   |   | Departamento |
| Restrição | Sou Operador logado Tento gerar o relatório |   | O sistema nega e oculta |
|   |   | com nomes reais | essa opção da interface |

*RF011 [Baixa] Relatório dividido por Departamento com posição, nome e tipo de vaga*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Exporto o relatório | O PDF é gerado | As Vagas aparecem |
|   |   |   | agrupadas por |
|   |   |   | Departamento com |
|   |   |   | posição, nome/nome |
|   |   |   | fantasia e tipo de vaga |
|   |   |   | (bolsista/voluntária) |
| Lista Espera Uma Vaga possui |   | O relatório é gerado Os Estudantes em |   |
|   | Lista de Espera |   | espera aparecem |
|   |   |   | identificados como |
|   |   |   | "Lista de Espera" após |
|   |   |   | os classificados nas |
|   |   |   | vagas |
| Bloqueio | Há empate não | Tento exportar o | O sistema alerta sobre |
|   | resolvido | relatório | o empate pendente e |
|   |   |   | bloqueia a exportação |

*RF012 [Baixa] Aviso de empate e resolução pelo Operador*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Dois Estudantes têm | A classificação é | O sistema exibe aviso |
|   | a soma IRA + Média | calculada | destacado para o |
|   | idêntica na mesma |   | Operador das turmas |
|   | Vaga (ex: |   | envolvidas |
|   | 8,0+8,0=16,0 e |   |   |
|   | 9,0+7,0=16,0) |   |   |
| Resolução | Há aviso de empate | O Operador clica no | A classificação é |
|   | ativo na tela | botão de resolução e | recalculada e o aviso é |
|   |   | seleciona qual | removido |
|   |   | Estudante fica na |   |
|   |   | frente |   |


| Bloqueio | Há empate não | O Operador tenta | O sistema bloqueia a |
| --- | --- | --- | --- |
|   | resolvido | exportar o relatório | exportação e indica os |
|   |   |   | empates pendentes |
| Borda | O empate envolve | O aviso é gerado | Ambos os Operadores |
|   | Estudantes de turmas |   | recebem o aviso e |
|   | de Operadores |   | qualquer um deles pode |
|   | diferentes (mesma |   | resolvê-lo |
|   | pontuação IRA + |   |   |
|   | Média) |   |   |

*RF013 [Alta] Administrador visualiza todos os Estudantes e Classificações*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Administrador | Acesso a listagem de | Vejo todos os |
|   | logado | Estudantes | Estudantes de todas as |
|   |   |   | Turmas |
| Sucesso | Sou Administrador | Acesso a tela de | Vejo a classificação |
|   | logado | Classificações | completa de todas as |
|   |   |   | Vagas e todos os |
|   |   |   | Departamentos |

*RF014 [Alta] Operadores visualizam apenas Estudantes de suas Turmas*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Operador da | Acesso a listagem de | Vejo apenas os |
|   | Turma A logado | Estudantes | Estudantes da Turma A |
| Restrição | Sou Operador da | Tento acessar dados | O sistema nega o |
|   | Turma A | de um Estudante da | acesso com erro de |
|   |   | Turma B diretamente | permissão |
|   |   | pela URL |   |

*RF015 [Média] Operadores podem ver todas as Vagas e Classificações*

| Cenário | Dado (contexto) | Quando (ação) | Então (resultado) |
| --- | --- | --- | --- |
| Sucesso | Sou Operador logado Acesso a tela de |   | Vejo todas as Vagas de |
|   |   | Vagas | todos os |
|   |   |   | Departamentos com |
|   |   |   | suas classificações |
| Distinção | Sou Operador e vejo | Visualizo os dados | Vejo posição e nome |
|   | a classificação de |   | fantasia, mas não os |
|   | uma Vaga com |   | dados pessoais (nome |
|   |   |   | real, matrícula, IRA) dos |


| Estudantes de outra | Estudantes de outras |
| --- | --- |
| Turma | Turmas |
