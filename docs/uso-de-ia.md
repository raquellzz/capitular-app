# Registro de uso de IA - Capitular

Este documento registra onde a equipe usou ferramentas de IA generativa no projeto, para quê, e o que foi decidido, implementado, revisado e testado pelos integrantes.

## Ferramentas

| Ferramenta | Quem usou | Para quê |
|---|---|---|
| Gemini (Google) | Raquel | Mapeamento de tarefas das sprints, divisão equilibrada do trabalho em dupla, esclarecimento de dúvidas técnicas e apoio na organização da documentação |
| Claude (Anthropic) | Raquel | Mapeamento de pendências frente ao código atual, resolução de problemas de sincronização/ambiente, tira-dúvidas pontuais e revisão de código antes da abertura de PRs |
<!-- | `[preencher]` | Pablo | `[preencher]` | -->

## Sprints 0 e 1

- **Planejamento e documentação:** O Gemini foi utilizado como apoio para estruturar a visão de produto, delimitar o escopo do MVP, quebrar as histórias de usuário (`US01` a `US05`) e organizar os cartões da Sprint 1 no GitHub Projects.
- **Ambiente e ferramentas:** Apoio no diagnóstico de erros de configuração de linters (`ktlint` e `detekt`) e limitações do emulador Android em sistema Linux imutável (Bluefin/Flatpak), levando à adoção de testes em dispositivo físico e execução local.

## Sprint 2 — Raquel

### Planejamento e mapeamento de tarefas

- **Como a IA auxiliou:** A partir dos guias da disciplina (`SPRINT-2.md` e `SPRINT-2-TAREFAS.md`), Gemini e Claude foram usados para mapear o que faltava para a entrega final, cruzar os requisitos com o estado real do repositório e da API, e propor uma divisão de tarefas em fatias verticais entre os dois integrantes.
- **O que a equipe decidiu:** A divisão final das responsabilidades, a criação e priorização das issues no GitHub Projects (incluindo a criação da issue #28 para os ViewModels do feed) e o adiamento da edição de grupo, que exigiria um novo endpoint no backend.

### Implementação, dúvidas e revisão pré-PR

Toda a implementação das funcionalidades e a escrita dos testes na frente de grupos, feed e check-in (**#15**, **#16**, **#17**, **#28** e **#18**) foram conduzidas pela Raquel. O fluxo de uso de IA durante o desenvolvimento foi de suporte:

1. **Tira-dúvidas técnicas:** Consultas pontuais sobre arquitetura em camadas, configuração de dependências (Koin, DataStore, Ktor Client e Turbine) e tradução de exceções de rede para falhas do domínio.
2. **Resolução de problemas de sync e ambiente:** Apoio na depuração de problemas de sincronização do Gradle e de conexão entre o celular físico e o backend local.
3. **Revisão pré-PR:** Antes de abrir os Pull Requests, o código final era revisado com apoio da IA para checar aderência aos critérios da rubrica, possíveis edge cases e limpeza dos imports do domínio.

| Issue | Escopo implementado pela integrante | Apoio pontual da IA |
|---|---|---|
| #15 Setup, camadas e dependências | Criação dos pacotes `domain`, `data`, `presentation` e `di`; configuração de Koin, DataStore, Ktor (`expectSuccess` e `ignoreUnknownKeys`) e falhas do domínio | Tira-dúvidas sobre configuração de dependências, resolução de erros de sync do Gradle e revisão pré-PR |
| #16 Grupo atual | Tela de criar/entrar em grupo, aba Grupos (convite, membros, troca e saída), destaque do grupo ativo no feed e melhorias de UX identificadas nos testes em aparelho | Discussão de escopo (adiamento da edição de grupo) e revisão final do código |
| #17 Repositório do feed com Ktor | DTOs, mapeamento para o domínio, upload multipart da foto, criação de check-in e testes com `MockEngine` | Tira-dúvidas sobre upload multipart no Ktor e revisão dos cenários de teste |
| #28 ViewModels e estados | `FeedViewModel` e `CheckInViewModel` com `StateFlow`, estados selados na tela, seletor de foto e remoção dos mocks do feed | Checagem de aderência à rubrica (uso de `when` sem `else` e estados obrigatórios) antes do PR |
| #18 Cache local | Repositórios com cache (padrão Decorator) usando DataStore, aviso de conteúdo desatualizado e limpeza de cache no logout | Tira-dúvidas sobre DataStore e revisão final do fluxo offline |

### Ambiente e conectividade

Durante os testes em dispositivo físico (Motorola Edge 40 Neo), a IA auxiliou no diagnóstico de por que o aplicativo não alcançava a API local: o app havia sido compilado com o endereço padrão do emulador (`10.0.2.2`). A solução adotada foi o uso de `adb reverse tcp:8000 tcp:8000` apontando para `localhost`, tornado configurável via propriedade `capitular.apiUrl`.

### Como o resultado foi verificado

- **Testes automatizados:** A suíte (`./gradlew :androidApp:testDebugUnitTest`) foi executada pela integrante a cada alteração, fechando com 69 testes passando (repositórios com `MockEngine`, cache com DataStore em arquivo temporário, ViewModels com Turbine e telas com Compose UI Test).
- **Testes no aparelho:** Fluxos de cadastro, criação/troca de grupo, check-in com foto persistido no backend e comportamento offline validados diretamente no celular.
<!-- - **Revisão entre pares:** Os commits e PRs foram abertos pela Raquel e revisados por `[preencher: Pablo]`. -->

### Limites e cuidados

- A IA atuou como ferramenta de apoio para planejamento, resolução de dúvidas/sync e revisão de código e não como substituta da implementação.
- Todas as decisões de arquitetura, escopo e interface foram tomadas pela integrante, que revisou, testou e compreende o código submetido.

## Sprint 2 — Pablo
