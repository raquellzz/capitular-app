# Proposta

## Visão de Produto

**Para** leitores casuais e estudantes \
**Que** perdem a consistência do hábito de leitura por falta de motivação ou por acharem os métodos de registro solitários \
**O** Capitular é um aplicativo móvel de gamificação social \
**Que** incentiva a leitura diária através de check-ins fotográficos rápidos e um placar de consistência (streaks) entre grupos de amigos \
**Diferente** de redes sociais literárias densas (como Skoob ou Goodreads) e planilhas individuais \
**Nosso** produto foca na validação visual rápida do progresso diário e garante que o usuário não perca sua ofensiva de leitura mesmo sem internet, sincronizando os check-ins offline automaticamente. 

## O Produto Mínimo Viável (MVP)

### Hipótese de Valor:

Acreditamos que leitores casuais vão registrar suas leituras diariamente porque a gamificação em formato de ranking e a comprovação por foto criam um senso de responsabilidade mútua e pertencimento ao grupo.  

### O que entra no MVP
- Cadastro de check-in diário com intervalo de páginas lidas e captura de foto pelo aplicativo.
- Tela de feed exibindo os check-ins recentes dos membros do grupo de leitura.
- Placar de classificação (ranking) semanal baseado no volume de páginas lidas.
- Funcionamento offline com fila local de check-ins e sincronização automática ao recuperar conexão.  

### O que fica fora do MVP
- Sistema de chat, comentários ou mensagens diretas entre os usuários.
- Reconhecimento óptico de caracteres (OCR) para validar o texto na foto da página.
- Integração com APIs externas (como Google Books) para buscar capas, autores e sinopses.
- Sistema complexo de conquistas, perfis detalhados e medalhas virtuais.

## Plataforma alvo

- Plataforma escolhida: Android
- Justificativa: O desenvolvimento terá o Android como alvo principal porque é o sistema operacional predominante entre estudantes universitários e jovens leitores (nosso público-alvo central). Além disso, como o produto (Capitular) depende do uso intenso da câmera para validação fotográfica diária dos check-ins, o ambiente Android oferece ferramentas de emulação e testes físicos locais mais acessíveis para a equipe garantir a estabilidade desse recurso de hardware antes de compilar para outras plataformas. O iOS foi descartado para o MVP devido à necessidade de hardware específico (macOS) para compilação e testes nativos contínuos da câmera.

## Estratégia de Backend

- Backend escolhido: API REST Própria (Python / FastAPI).
- Justificativa: Optamos por construir uma API própria utilizando FastAPI pela altíssima velocidade de prototipação e geração automática de documentação (Swagger). Essa escolha nos permite ter controle estrutural sobre o banco de dados relacional e a lógica de gamificação, aproveitando uma tecnologia de execução rápida e de fácil manutenção. O uso de serviços de Backend-as-a-Service (como Firebase) foi descartado porque desejamos focar o aprendizado no desafio técnico de implementar a fila de sincronização offline de ponta a ponta, lidando manualmente com a persistência de dados no dispositivo cliente e as requisições HTTP para nosso servidor.