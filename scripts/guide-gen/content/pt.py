# -*- coding: utf-8 -*-
"""Guia em português (traduzido de ko.py)."""

UI = dict(crumb="Guia de corrida", author="Equipe Pace League", date="30 de setembro de 2026", related="Mais guias", read_in="Ler em")

HUB = dict(title="Guia de corrida",
           desc="Cálculo de ritmo, plano de 5 km em 8 semanas para iniciantes, prevenção de lesões e estratégia do Landeat — guias de corrida da Pace League.",
           lead="Seja para quem está começando ou para quem quer baixar o recorde pessoal, reunimos aqui o que você precisa para correr com constância e prazer. Também mostramos como aproveitar melhor os rankings e o Landeat da Pace League.")

ARTICLES = {
  'pace': dict(tag='Básico', title='Entendendo o ritmo de corrida: do cálculo ao ritmo por intensidade de treino',
       desc='O que significa o ritmo (min/km), como calculá-lo, como convertê-lo em velocidade, tabelas de tempo para 5 km, 10 km, meia e maratona, e como definir o ritmo de rodagens leves, tempo run e intervalados.',
       summary='A relação entre ritmo e velocidade, uma tabela de tempos de chegada e o ritmo certo para rodagem leve, tempo run e intervalados.'),
  'first-5k': dict(tag='Iniciante', title='Plano de 8 semanas para correr seus primeiros 5 km',
       desc='Um plano semana a semana que combina caminhada e corrida para que até quem quase não se exercita consiga correr 5 km sem parar em 8 semanas, com dicas práticas.',
       summary='Comece com intervalos de caminhada e corrida e termine 8 semanas depois correndo 5 km sem pausa.'),
  'injury-prevention': dict(tag='Saúde', title='Prevenindo lesões na corrida: aquecimento, desaquecimento e como aumentar a quilometragem',
       desc='Causas e prevenção das lesões mais comuns em joelho, canela e pé, rotinas de aquecimento e desaquecimento, como aumentar a quilometragem semanal com segurança e quando trocar o tênis.',
       summary='Lesões comuns na corrida, rotinas de aquecimento e desaquecimento e como aumentar a quilometragem semanal com segurança.'),
  'landit-strategy': dict(tag='Landeat', title='Estratégia do Landeat: rotas que conquistam mais terreno com a mesma distância',
       desc='As regras de conquista do Landeat, o jogo de corrida da Pace League, como planejar rotas que cobrem mais área com a mesma distância e como tomar terreno de outros corredores.',
       summary='Quando o terreno é validado, qual formato de circuito maximiza a área e como tomar terreno de outros corredores.'),
}

BODY = {}

BODY['pace'] = """
    <p class="guide-lead">
      O ritmo é o primeiro número que você vê ao abrir um app de corrida. Mas muitas vezes é difícil saber se um "ritmo de 5:30" é rápido
      ou lento, ou em que ritmo você deveria correr hoje. Este guia explica o que é o ritmo, como calculá-lo, como transformar um tempo-alvo
      em ritmo e como variar o ritmo de acordo com o objetivo de cada treino.
    </p>

    <h2>Ritmo: o tempo para correr 1 km</h2>
    <p>
      Na corrida, ritmo (pace) é <strong>o tempo que você leva para percorrer 1 km</strong>, normalmente escrito em minutos e segundos, como
      <code>5'30"/km</code>. Corredores usam o ritmo em vez da velocidade (km/h) porque ele é mais intuitivo: se você quer correr 10 km em
      menos de 55 minutos, sabe na hora que precisa passar cada quilômetro em 5 minutos e 30 segundos ou menos.
    </p>
    <p>
      No ritmo, <strong>quanto menor o número, mais rápido</strong>. Um ritmo de 5 minutos é um minuto por quilômetro mais rápido que um de 6.
      Quem está começando costuma correr entre 7 e 8 minutos por km, enquanto corredores amadores que treinam com regularidade muitas vezes correm
      confortavelmente por volta dos 5 minutos. O ritmo varia muito com peso, idade, altimetria do percurso e clima, então ele é mais útil para
      acompanhar <strong>a evolução dos seus próprios registros</strong> do que para se comparar com os outros.
    </p>

    <h2>Como calcular o ritmo e converter em velocidade</h2>
    <p>Ritmo = <strong>tempo total ÷ distância (km)</strong>.</p>
    <ul>
      <li>5 km em 30 minutos: 30 ÷ 5 = <strong>6:00 por km</strong></li>
      <li>10 km em 58 minutos: 58 ÷ 10 = 5,8 minutos = <strong>5:48 por km</strong> (0,8 × 60 = 48 segundos)</li>
      <li>3,2 km em 20 minutos: 20 ÷ 3,2 = 6,25 minutos = <strong>6:15 por km</strong></li>
    </ul>
    <p>
      Velocidade e ritmo se relacionam assim: <strong>ritmo (minutos) = 60 ÷ velocidade (km/h)</strong>. Uma esteira a 10 km/h equivale a
      60 ÷ 10 = ritmo de 6 minutos; a 12 km/h, ritmo de 5 minutos. Útil para comparar a esteira com o ritmo ao ar livre.
    </p>

    <h2>Tempos de chegada por ritmo</h2>
    <p>Se você tem um tempo-alvo para uma prova, encontre na tabela o ritmo médio necessário. (Meia maratona 21,0975 km, maratona 42,195 km)</p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Ritmo (/km)</th><th>Velocidade</th><th>5 km</th><th>10 km</th><th>Meia</th><th>Maratona</th></tr>
        <tr><td>4'30"</td><td>13,3 km/h</td><td>22:30</td><td>45:00</td><td>1:34:56</td><td>3:09:53</td></tr>
        <tr><td>5'00"</td><td>12,0 km/h</td><td>25:00</td><td>50:00</td><td>1:45:29</td><td>3:30:59</td></tr>
        <tr><td>5'30"</td><td>10,9 km/h</td><td>27:30</td><td>55:00</td><td>1:56:02</td><td>3:52:04</td></tr>
        <tr><td>6'00"</td><td>10,0 km/h</td><td>30:00</td><td>1:00:00</td><td>2:06:35</td><td>4:13:10</td></tr>
        <tr><td>6'30"</td><td>9,2 km/h</td><td>32:30</td><td>1:05:00</td><td>2:17:08</td><td>4:34:16</td></tr>
        <tr><td>7'00"</td><td>8,6 km/h</td><td>35:00</td><td>1:10:00</td><td>2:27:41</td><td>4:55:22</td></tr>
      </table>
    </div>
    <p>
      Uma diferença de 30 segundos no ritmo vira cerca de 21 minutos numa maratona. Quanto mais longa a prova, mais caro sai na segunda metade
      ter começado um pouco rápido demais; por isso, depois de definir seu ritmo-alvo, não corra mais rápido que ele no início.
    </p>

    <h2>Como definir o ritmo por objetivo de treino</h2>
    <p>
      Correr sempre no mesmo ritmo tende a acumular cansaço sem muita evolução. Dividir o treino por intensidade faz o mesmo tempo investido
      render muito mais. As referências abaixo são aproximadas e se baseiam no seu ritmo recente de 5 km.
    </p>
    <h3>Rodagem leve (a base da maior parte da sua corrida)</h3>
    <p>
      Um esforço confortável em que você consegue falar frases completas — cerca de 1 a 1,5 minuto por km mais lento que o seu ritmo de 5 km.
      Se você corre 5 km em 30 minutos (ritmo 6:00), seu ritmo leve fica em torno de 7:00–7:30. Deve parecer quase lento demais; essa é a
      intensidade certa, que desenvolve resistência aeróbica e capacidade de recuperação. Muitas abordagens de treino recomendam passar cerca
      de 80% do tempo de corrida nessa intensidade leve.
    </p>
    <h3>Tempo run (confortavelmente difícil)</h3>
    <p>
      Um esforço em que dá para responder com frases curtas, mas não manter uma conversa, sustentado por 20 a 30 minutos. Mire em cerca de
      20 a 30 segundos por km mais lento que o seu ritmo de 5 km. Desenvolve a capacidade de manter uma velocidade constante por muito tempo,
      o que ajuda nos 10 km e na meia maratona.
    </p>
    <h3>Intervalados (repetições curtas e rápidas)</h3>
    <p>
      Corra de 400 m a 1 km no seu ritmo de 5 km ou um pouco mais rápido, recupere caminhando ou trotando devagar pelo mesmo tempo e repita
      de 4 a 8 vezes. Como são intensos, uma vez por semana costuma bastar, e iniciantes fazem melhor construindo antes uma base com rodagens leves.
    </p>

    <h2>Por que o ritmo do GPS oscila</h2>
    <p>
      Você provavelmente já viu seu ritmo atual saltar de repente para 3 ou 10 minutos por km no meio da corrida. O GPS do celular perde precisão
      entre prédios altos, sob viadutos e embaixo de árvores densas, então o ritmo instantâneo em trechos curtos pode estar errado.
      <strong>As parciais por quilômetro ou o ritmo médio total</strong> são mais confiáveis. Correr o mesmo percurso repetidamente também facilita
      as comparações, porque os erros típicos do percurso aparecem de forma parecida a cada vez.
    </p>

    <div class="guide-note">
      <p>
        <strong>Na Pace League,</strong> seu ritmo médio é calculado automaticamente a partir da distância e do tempo do GPS ao terminar a corrida,
        e uma pontuação que considera distância e ritmo é somada ao seu ranking da temporada e ao seu nível. Melhorar o ritmo aumenta a pontuação
        para a mesma distância, então construa sua base com rodagens leves e inclua de vez em quando um tempo run ou intervalados.
      </p>
    </div>
"""

BODY['first-5k'] = """
    <p class="guide-lead">
      Mesmo que você nunca tenha corrido 5 km sem parar, oito semanas bastam para chegar lá. O segredo é não tentar correr sem parar desde o
      primeiro dia. Este plano alterna caminhada e corrida e aumenta aos poucos o tempo correndo, para que na semana 8 você consiga correr
      30 minutos seguidos.
    </p>

    <h2>Três coisas para saber antes de começar</h2>
    <ol>
      <li><strong>Esqueça a velocidade.</strong> Corra devagar o bastante para ainda conseguir dizer uma frase curta, não tão forte a ponto de não conseguir falar. No início pode parecer pouco mais que uma caminhada rápida — e tudo bem.</li>
      <li><strong>Treine três vezes por semana, com um dia de descanso entre os treinos.</strong> Segunda/quarta/sexta ou terça/quinta/sábado funcionam bem. Músculos e articulações se adaptam e ficam mais fortes nos dias de descanso.</li>
      <li><strong>Caminhe 5 minutos antes e depois de cada treino.</strong> Os tempos da tabela são só do treino principal; acrescente 5 minutos de caminhada rápida no início e no fim.</li>
    </ol>

    <h2>Plano semana a semana</h2>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Semana</th><th>Treino principal (3× por semana)</th><th>Tempo total correndo</th></tr>
        <tr><td>1</td><td>Correr 1 min + caminhar 2 min × 8</td><td>8 min</td></tr>
        <tr><td>2</td><td>Correr 2 min + caminhar 2 min × 6</td><td>12 min</td></tr>
        <tr><td>3</td><td>Correr 3 min + caminhar 2 min × 5</td><td>15 min</td></tr>
        <tr><td>4</td><td>Correr 5 min + caminhar 2 min × 4</td><td>20 min</td></tr>
        <tr><td>5</td><td>Correr 8 min + caminhar 2 min × 3</td><td>24 min</td></tr>
        <tr><td>6</td><td>Correr 12 min + caminhar 2 min × 2</td><td>24 min</td></tr>
        <tr><td>7</td><td>Correr 20 min seguidos (22 min no 3º treino)</td><td>20–22 min</td></tr>
        <tr><td>8</td><td>Correr 25 min seguidos → no último treino, tente 5 km ou 30 min</td><td>25–30 min</td></tr>
      </table>
    </div>
    <p>
      Se uma semana ficar difícil demais, <strong>simplesmente repita-a</strong>. Não há problema algum se as oito semanas virarem dez ou doze.
      Por outro lado, pular semanas porque algo parece fácil não é recomendado. Coração e pulmões melhoram rápido, mas articulações e tendões
      se adaptam mais devagar, e forçar só porque a respiração ficou mais fácil costuma levar a dores no joelho ou na canela.
    </p>

    <h2>Pontos-chave de cada fase</h2>
    <h3>Semanas 1–2: criar o hábito</h3>
    <p>
      O objetivo aqui não é condicionamento, e sim <strong>o hábito de sair nos dias combinados</strong>. Se ficar sem fôlego nos trechos de 1–2 minutos
      de corrida, diminua ainda mais o ritmo. Os trechos de caminhada fazem parte do treino, não são descanso — continue caminhando sem parar.
    </p>
    <h3>Semanas 3–5: aumentar os trechos de corrida</h3>
    <p>
      Quando os trechos de corrida chegam a 3, 5 e 8 minutos, você vai sentir esforço de verdade pela primeira vez. Respire pelo nariz e pela boca,
      solte ombros e mãos e encurte a passada para que os pés toquem o chão mais perto do corpo — faz muita diferença.
    </p>
    <h3>Semanas 6–8: correr sem parar</h3>
    <p>
      Na semana 7 você corre 20 minutos sem parar pela primeira vez. É a maior barreira mental, mas nas semanas 5–6 você já correu duas vezes
      12 minutos (24 minutos no total), então o condicionamento está lá. O segredo é começar os primeiros 10 minutos ainda mais devagar que o normal.
      No último treino da semana 8, tente chegar a 5 km ou 30 minutos, o que vier primeiro.
      Os primeiros 5 km em 35–40 minutos são um resultado excelente para um iniciante.
    </p>

    <h2>Dicas para não desistir</h2>
    <ul>
      <li><strong>Confira primeiro o tênis.</strong> Um tênis de corrida com amortecimento sobrecarrega menos as articulações de um iniciante que um tênis casual de sola fina. Experimente na loja e deixe cerca de um dedo polegar de espaço à frente dos dedos.</li>
      <li><strong>Registre seus treinos.</strong> Anotar a data, o tempo corrido e como você se sentiu permite ver o quanto tudo ficou mais fácil depois de 2–3 semanas — a melhor motivação que existe.</li>
      <li><strong>Não corra com dor.</strong> Dor muscular é normal, mas se um ponto doer de forma aguda ou você começar a mancar, pare e descanse alguns dias. Se a dor continuar, procure um médico.</li>
      <li><strong>Tenha um plano B para o mau tempo.</strong> Em dias de chuva ou ar poluído, a esteira ou uma caminhada rápida mantêm o hábito.</li>
    </ul>

    <div class="guide-note">
      <p>
        <strong>Na Pace League,</strong> ao registrar sua corrida no app, a distância e o ritmo médio são calculados automaticamente e salvos no seu
        histórico, e sua pontuação de temporada e seu nível sobem conforme você corre. Acompanhe sua evolução ao longo das oito semanas e, no dia em
        que completar seus primeiros 5 km, compartilhe no mural de comprovação da comunidade com a corrida anexada.
      </p>
    </div>
"""

BODY['injury-prevention'] = """
    <p class="guide-lead">
      Correr não exige equipamento especial, mas, como o mesmo movimento se repete milhares de vezes, lesões por sobrecarga são comuns.
      Felizmente, muitas lesões de corrida podem ser reduzidas só de prestar atenção à velocidade com que você aumenta a quilometragem e ao
      aquecimento e desaquecimento. Este guia mostra as causas das lesões mais comuns e hábitos que você pode começar hoje.
    </p>

    <div class="guide-note">
      <p>Este artigo traz informações gerais sobre exercício e não substitui diagnóstico ou tratamento médico. Se a dor persistir ou houver inchaço, consulte um médico.</p>
    </div>

    <h2>Lesões comuns em corredores</h2>
    <ul>
      <li><strong>Dor na frente do joelho</strong>: costuma ser um incômodo em volta da patela ou dor ao descer escadas. Conhecida como "joelho de corredor", muitas vezes está ligada à fraqueza de coxas e quadris ou a um aumento brusco da quilometragem.</li>
      <li><strong>Dor na canela</strong>: uma dor latejante ao longo da borda interna da tíbia, comum quando se começa a correr ou quando se aumenta de repente a distância em superfícies duras.</li>
      <li><strong>Dor na sola do pé</strong>: se a parte de baixo do calcanhar pinica nos primeiros passos da manhã, pode ser sinal de sobrecarga na fáscia plantar.</li>
      <li><strong>Dor no tendão de Aquiles</strong>: rigidez e dor no tendão acima do calcanhar, que costuma aparecer após um aumento repentino de subidas ou treinos de velocidade.</li>
    </ul>
    <p>
      O que essas lesões têm em comum é que <strong>a carga de treino aumentou mais rápido do que o corpo conseguiu se adaptar</strong>.
      O condicionamento aeróbico melhora em semanas, mas ossos, tendões e ligamentos levam meses para se adaptar. Aumentar distância e velocidade
      ao mesmo tempo só porque a respiração ficou mais fácil abre justamente essa lacuna.
    </p>

    <h2>Aumente a distância devagar, uma coisa de cada vez</h2>
    <p>
      Uma regra prática muito usada entre corredores é <strong>aumentar a distância semanal total em no máximo cerca de 10% em relação à semana anterior</strong>.
      Se você correu 20 km nesta semana, algo em torno de 22 km na próxima é razoável. Não é um limite científico exato, mas funciona bem como proteção
      contra saltos bruscos. Outros princípios relacionados:
    </p>
    <ul>
      <li><strong>Não aumente distância e intensidade ao mesmo tempo.</strong> Na semana em que aumentar a distância, não acrescente intervalados nem subidas.</li>
      <li><strong>A cada 3 ou 4 semanas, faça uma semana mais leve.</strong> Reduza a distância semanal em 20–30% para dar tempo ao corpo de se recuperar.</li>
      <li><strong>Limite os dias seguidos de corrida.</strong> Para iniciantes, é bom intercalar um dia de descanso ou uma caminhada leve entre as corridas.</li>
    </ul>

    <h2>Rotina de aquecimento de 5–10 minutos</h2>
    <p>
      Acelerar com os músculos frios aumenta o risco de lesão. Antes de correr, um aquecimento dinâmico que movimenta as articulações em toda a sua
      amplitude é mais adequado do que o alongamento estático (manter uma posição por muito tempo).
    </p>
    <ol>
      <li>3–5 minutos de caminhada rápida ou trote bem leve</li>
      <li>Balanço de pernas: apoiado na parede, 10 para frente e para trás e 10 de lado a lado em cada perna</li>
      <li>Avanço caminhando: 10 passos largos</li>
      <li>Elevação de joelhos e calcanhar no glúteo: 20 segundos cada, no lugar</li>
      <li>Corra o primeiro quilômetro do treino mais devagar que o seu objetivo</li>
    </ol>

    <h2>Desaquecimento e recuperação</h2>
    <p>
      Em vez de parar de uma vez ao terminar, caminhe ou trote devagar por 3–5 minutos para baixar os batimentos e depois alongue de forma estática
      panturrilhas, parte da frente e de trás das coxas e glúteos por 20–30 segundos cada. Dormir bem, se hidratar e incluir proteínas e carboidratos
      na refeição pós-corrida também ajudam na recuperação para o próximo treino.
    </p>

    <h2>10 minutos de fortalecimento, duas vezes por semana</h2>
    <p>
      Só correr não deixa fortes o suficiente os músculos de quadril, coxas e panturrilhas que absorvem o impacto da pisada.
      Investir apenas 10 minutos nos dias sem corrida ajuda a reduzir a sobrecarga em joelhos e tornozelos.
    </p>
    <ul>
      <li>Agachamento: 3 séries de 15</li>
      <li>Elevação de quadril (deitado, levante o quadril): 3 séries de 15</li>
      <li>Elevação de panturrilha: 3 séries de 20</li>
      <li>Prancha: 3 × 30 segundos</li>
    </ul>

    <h2>Quando trocar o tênis de corrida</h2>
    <p>
      O amortecimento do tênis se desgasta aos poucos e sem que se perceba. Costuma-se citar <strong>cerca de 500 a 800 km</strong> como momento de troca,
      e ele pode se desgastar antes dependendo do peso e da forma de correr. Se a sola estiver muito gasta de um lado, ou se uma dor nova nas pernas
      sumir ao trocar por um par novo, você já tinha passado do ponto. Registrar a quilometragem ajuda a estimar esse momento.
    </p>

    <h2>Quando é hora de descansar</h2>
    <ul>
      <li>Um ponto específico dói ao ser pressionado e a dor piora quanto mais você corre</li>
      <li>Há inchaço ou você começa a mancar</li>
      <li>A dor não diminui depois de vários dias de descanso</li>
    </ul>
    <p>
      Alguns dias de descanso quase não afetam seu condicionamento, mas continuar correndo com dor até a lesão piorar pode custar semanas ou meses parado.
    </p>

    <div class="guide-note">
      <p>
        <strong>Na Pace League,</strong> suas corridas ficam registradas por data, então é fácil comparar a distância total desta semana com a da anterior.
        Ao aumentar a distância, confira o aumento semanal na tela de registros e planeje sem exagerar.
      </p>
    </div>
"""

BODY['landit-strategy'] = """
    <p class="guide-lead">
      O Landeat é o jogo de corrida da Pace League em que sua rota conquista terreno em um mapa real. Mesmo correndo os mesmos 5 km, a área
      conquistada pode variar várias vezes dependendo do formato da rota. Este guia explica quando o terreno é validado, como planejar rotas que
      maximizem a área e como tomar terreno de outros corredores.
    </p>

    <h2>Quando o terreno é validado</h2>
    <p>Comece a corrida com o modo Landeat ativado; se todas as condições abaixo forem atendidas, o terreno é criado no momento em que a corrida termina.</p>
    <ul>
      <li><strong>Você precisa voltar ao ponto de partida.</strong> O ponto final deve estar a menos de 50 m do início para que a rota conte como um circuito fechado.</li>
      <li><strong>O perímetro precisa ter pelo menos 300 m.</strong> Circuitos curtos demais não contam.</li>
      <li><strong>A área precisa estar entre 10.000 m² (cerca de 100 m × 100 m) e 5 km².</strong> Circuitos pequenos demais ou anormalmente grandes são excluídos.</li>
    </ul>
    <p>
      Passadas essas verificações, a área cercada pelo seu circuito é convertida em <strong>peças hexagonais</strong> no mapa. Você ganha não só as
      peças dentro da rota, mas também todas as que a rota atravessa. Aproximando o mapa o suficiente, você vê essa grade hexagonal e pode conferir,
      peça por peça, onde começa e termina o terreno de cada corredor.
    </p>

    <h2>Mais área com a mesma distância: o formato é tudo</h2>
    <p>
      Com o mesmo perímetro, quanto mais o formato se aproxima de um círculo, mais área ele cerca. Por exemplo, para um circuito de 1,2 km:
    </p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Formato do circuito (perímetro 1,2 km)</th><th>Área cercada</th></tr>
        <tr><td>Retângulo de 100 m × 500 m</td><td>cerca de 50.000 m²</td></tr>
        <tr><td>Quadrado de 300 m × 300 m (um quarteirão)</td><td>cerca de 90.000 m²</td></tr>
        <tr><td>Círculo com raio de cerca de 191 m</td><td>cerca de 115.000 m²</td></tr>
      </table>
    </div>
    <p>
      Com os mesmos 1,2 km, um circuito longo e estreito conquista menos da metade do terreno de um circular. Nas ruas não dá para correr um círculo
      perfeito, então <strong>dar a volta em um quarteirão de largura e comprimento parecidos</strong> é a escolha mais prática.
      Rotas de ida e volta quase não cercam área e não geram terreno, então evite-as.
    </p>
    <p>
      Vale lembrar que uma volta numa pista de atletismo de 400 m cerca apenas cerca de 10.000 m², bem no limite mínimo. Um pequeno erro de GPS pode
      deixá-la abaixo do limite, então o contorno de um parque ou de um quarteirão residencial é mais seguro que a pista.
    </p>

    <h2>Um circuito grande ou vários pequenos</h2>
    <p>
      A área cresce com o quadrado do perímetro: dobre o perímetro e a área fica cerca de quatro vezes maior. Por isso um circuito de 3 km conquista
      muito mais terreno do que três de 1 km. Se você tem fôlego, uma volta grande pelo bairro é o melhor para o ranking de área.
      Um único circuito acima de 5 km² não conta, mas isso equivale a um quadrado de cerca de 2,2 km de lado, então em corridas normais quase nunca acontece.
    </p>

    <h2>Tomando terreno de outros corredores</h2>
    <p>
      Quando seu novo circuito se sobrepõe ao terreno de outra pessoa, <strong>as peças hexagonais sobrepostas</strong> passam a ser suas. A regra é simples:
      <strong>o último corredor a passar por uma peça é o dono dela</strong>. Não importa quem conquistou primeiro nem por quanto tempo a manteve.
    </p>
    <ul>
      <li>Não é preciso cobrir todo o território do outro. Mesmo uma sobreposição parcial dá a você todas as peças sobrepostas, e o território dele encolhe para as peças que restam.</li>
      <li>Se você cobrir todas as peças do território de alguém, ele desaparece do mapa.</li>
      <li>Peças que já são suas continuam iguais quando você passa por elas de novo; só as novas peças vazias e as conquistadas formam o seu novo terreno.</li>
    </ul>
    <p>
      O outro lado é que o seu terreno também pode ser tomado a qualquer momento. Se há outros corredores ativos perto das suas rotas habituais, repita
      o mesmo circuito regularmente para recuperar as peças perdidas. O ranking do Landeat na tela do mapa mostra a área total de cada corredor, então
      ver onde fica o terreno dos primeiros colocados também ajuda a planejar suas rotas.
    </p>

    <h2>Checklist para sua corrida valer</h2>
    <ol>
      <li>Antes de começar, confira se o modo Landeat está ativado.</li>
      <li>Memorize o ponto de partida e sempre toque em finalizar a menos de 50 m dele.</li>
      <li>Evite circuitos em que a maior parte da rota passe por áreas de GPS fraco, como entre prédios altos ou por túneis.</li>
      <li>Escolha uma rota que gire em um único sentido em vez de ida e volta.</li>
    </ol>

    <div class="guide-note">
      <p>
        <strong>Segurança em primeiro lugar.</strong> Não atravesse vias de carros, não entre em propriedades privadas ou obras com acesso restrito e não corra
        à noite em lugares desertos só para ganhar mais terreno. Todo o terreno pode ser conquistado usando apenas ruas e calçadas públicas.
      </p>
    </div>
"""


# ---------------------------------------------------------------- 소개 페이지 (/about)
ABOUT = {'title': 'Sobre', 'desc': 'A Pace League é um serviço de corrida que reúne registro de corridas com cálculo de ritmo e calorias, rankings e níveis, o jogo de conquista de território Landeat, crews de corrida e uma comunidade.', 'h1': 'Pace League: um serviço de corrida que dá um motivo para você correr todos os dias', 'lead': 'A Pace League é mais do que um app que registra quanto você correu. Ela transforma cada corrida em pontuação e nível para você competir, permite conquistar terreno em um mapa real com sua rota de GPS, formar crews para correr junto e compartilhar corridas e histórias com outros corredores — tudo pensado para transformar “correr sozinho” em “correr junto, e com constância”.', 'sections': [('clock', 'Registro de corridas com cálculo automático de ritmo e calorias', 'Ao iniciar uma corrida no app, suas coordenadas de GPS são enviadas ao servidor em tempo real e salvas como uma única sessão. Ao terminar, seu ritmo médio (min/km) e as calorias gastas são calculados automaticamente a partir da distância e do tempo e adicionados ao seu histórico. Sem calculadora e sem digitar nada — é só correr que os registros se acumulam.'), ('trophy', 'Rankings e níveis', 'Suas corridas são convertidas em uma pontuação que considera distância e ritmo, e a cada temporada essa pontuação coloca você em um nível do Bronze até o mais alto. Além do ranking completo da temporada, um TOP 10 logo na tela inicial mostra num relance onde você está entre os outros corredores.'), ('pin', 'Landeat — um jogo de corrida para conquistar terreno', 'O Landeat é o jogo de corrida baseado em mapa exclusivo da Pace League. Corra um circuito fechado com uma distância mínima que volte ao ponto de partida, e a área cercada pela sua rota vira peças hexagonais (terreno) em um mapa real que passam a ser suas. Se a sua rota se sobrepuser a um terreno que outro corredor já tem, você pode tomar essa parte — então, mesmo correndo no mesmo bairro, cada vez exige uma estratégia diferente. Na tela do mapa você vê o seu terreno, o de outros corredores e o ranking do Landeat por área total.'), ('users', 'Crews — equipes para correr junto', 'Se você prefere correr em equipe em vez de sozinho, pode criar uma crew ou entrar em uma existente. Cada pessoa participa de apenas uma crew, conduzida por um líder que convida outros corredores ou aprova pedidos de entrada. Ao entrar, o selo da sua crew aparece nas suas publicações e nos rankings, e os outros corredores veem naturalmente de qual crew você faz parte.'), ('chat', 'Comunidade', 'A comunidade é dividida em murais de conversa livre, perguntas, comprovação de corridas e divulgação de crews, onde você pode compartilhar corridas, estratégias do Landeat e novidades da crew. Dá para anexar fotos e vídeos direto no editor e incluir sua própria corrida em uma publicação como comprovação. Qualquer pessoa pode ver os murais e ler as publicações sem login; só escrever, comentar e votar exigem conta.')], 'cta': 'Registre hoje a sua primeira corrida.', 'join': 'Cadastrar-se', 'login': 'Entrar', 'links': {'guide': 'Guia de corrida', 'privacy': 'Política de Privacidade', 'terms': 'Termos de Serviço', 'location': 'Termos do serviço baseado em localização'}}
