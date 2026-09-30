# -*- coding: utf-8 -*-
"""Guía en español (traducida de ko.py)."""

UI = dict(crumb="Guía de running", author="Equipo de Pace League", date="30 de septiembre de 2026", related="Más guías", read_in="Leer en")

HUB = dict(title="Guía de running",
           desc="Cálculo del ritmo, plan de 5 km en 8 semanas para principiantes, prevención de lesiones y estrategia de Landeat: guías de running de Pace League.",
           lead="Tanto si estás dando tus primeros pasos como si quieres mejorar tu marca personal, aquí encontrarás lo necesario para correr con constancia y disfrutar. También te contamos cómo sacar más partido a las clasificaciones y a Landeat de Pace League.")

ARTICLES = {
  'pace': dict(tag='Básico', title='Entender el ritmo de carrera: del cálculo al ritmo según la intensidad',
       desc='Qué significa el ritmo (min/km), cómo se calcula, cómo se convierte en velocidad, tablas de tiempos para 5K, 10K, media maratón y maratón, y cómo fijar el ritmo de rodajes suaves, tempo e intervalos.',
       summary='La relación entre ritmo y velocidad, una tabla de tiempos finales y el ritmo adecuado para rodajes suaves, tempo e intervalos.'),
  'first-5k': dict(tag='Principiantes', title='Plan de 8 semanas para correr tus primeros 5 km',
       desc='Un plan semana a semana que combina caminar y correr para que incluso quien apenas hace ejercicio pueda correr 5 km sin parar en 8 semanas, con consejos prácticos.',
       summary='Empieza con intervalos de caminar y correr y termina 8 semanas después corriendo 5 km sin pausa.'),
  'injury-prevention': dict(tag='Salud', title='Prevenir lesiones al correr: calentamiento, vuelta a la calma y cómo aumentar el kilometraje',
       desc='Causas y prevención de las lesiones de rodilla, espinilla y pie más comunes, rutinas de calentamiento y vuelta a la calma, cómo aumentar el kilometraje semanal con seguridad y cuándo cambiar las zapatillas.',
       summary='Lesiones habituales, rutinas de calentamiento y vuelta a la calma, y cómo aumentar el kilometraje semanal con seguridad.'),
  'landit-strategy': dict(tag='Landeat', title='Estrategia de Landeat: rutas que conquistan más terreno con la misma distancia',
       desc='Las reglas de conquista de Landeat, el juego de running de Pace League, cómo diseñar rutas que abarquen más terreno con la misma distancia y cómo arrebatar terreno a otros corredores.',
       summary='Cuándo cuenta el terreno, qué forma de circuito maximiza el área y cómo quitar terreno a otros corredores.'),
}

BODY = {}

BODY['pace'] = """
    <p class="guide-lead">
      El ritmo es el primer número que ves al abrir una app de running. Pero a menudo cuesta saber si un «ritmo de 5:30»
      es rápido o lento, o a qué ritmo deberías correr hoy. Esta guía explica qué significa el ritmo, cómo calcularlo,
      cómo convertir un tiempo objetivo en ritmo y cómo variar el ritmo según el objetivo de cada entrenamiento.
    </p>

    <h2>El ritmo: el tiempo que tardas en correr 1 km</h2>
    <p>
      En running, el ritmo es <strong>el tiempo que tardas en recorrer 1 km</strong>, y se suele expresar en minutos y segundos,
      como <code>5'30"/km</code>. Los corredores usan el ritmo en lugar de la velocidad (km/h) porque es más intuitivo: si quieres
      correr 10 km en menos de 55 minutos, sabes al instante que debes pasar cada kilómetro en 5 minutos 30 segundos o menos.
    </p>
    <p>
      En el ritmo, <strong>cuanto más pequeño el número, más rápido</strong>. Un ritmo de 5 minutos es un minuto por kilómetro más rápido
      que uno de 6. Quien empieza a correr suele ir a 7–8 minutos por km, mientras que los corredores populares que entrenan con
      regularidad suelen correr cómodos alrededor de 5 minutos. El ritmo varía mucho con el peso, la edad, el desnivel del recorrido
      y el clima, así que es más útil para seguir <strong>la evolución de tus propios registros</strong> que para compararte con otros.
    </p>

    <h2>Cómo calcular el ritmo y convertirlo en velocidad</h2>
    <p>El ritmo es <strong>tiempo total ÷ distancia (km)</strong>.</p>
    <ul>
      <li>5 km en 30 minutos: 30 ÷ 5 = <strong>6:00 por km</strong></li>
      <li>10 km en 58 minutos: 58 ÷ 10 = 5,8 minutos = <strong>5:48 por km</strong> (0,8 × 60 = 48 segundos)</li>
      <li>3,2 km en 20 minutos: 20 ÷ 3,2 = 6,25 minutos = <strong>6:15 por km</strong></li>
    </ul>
    <p>
      Velocidad y ritmo se relacionan así: <strong>ritmo (minutos) = 60 ÷ velocidad (km/h)</strong>. Una cinta a 10 km/h equivale a
      60 ÷ 10 = ritmo de 6 minutos; a 12 km/h, ritmo de 5 minutos. Resulta útil para comparar la cinta con el ritmo al aire libre.
    </p>

    <h2>Tiempos finales según el ritmo</h2>
    <p>Si tienes un tiempo objetivo para una carrera, busca en la tabla el ritmo medio que necesitas. (Media maratón 21,0975 km; maratón 42,195 km)</p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Ritmo (/km)</th><th>Velocidad</th><th>5K</th><th>10K</th><th>Media</th><th>Maratón</th></tr>
        <tr><td>4'30"</td><td>13,3 km/h</td><td>22:30</td><td>45:00</td><td>1:34:56</td><td>3:09:53</td></tr>
        <tr><td>5'00"</td><td>12,0 km/h</td><td>25:00</td><td>50:00</td><td>1:45:29</td><td>3:30:59</td></tr>
        <tr><td>5'30"</td><td>10,9 km/h</td><td>27:30</td><td>55:00</td><td>1:56:02</td><td>3:52:04</td></tr>
        <tr><td>6'00"</td><td>10,0 km/h</td><td>30:00</td><td>1:00:00</td><td>2:06:35</td><td>4:13:10</td></tr>
        <tr><td>6'30"</td><td>9,2 km/h</td><td>32:30</td><td>1:05:00</td><td>2:17:08</td><td>4:34:16</td></tr>
        <tr><td>7'00"</td><td>8,6 km/h</td><td>35:00</td><td>1:10:00</td><td>2:27:41</td><td>4:55:22</td></tr>
      </table>
    </div>
    <p>
      Una diferencia de 30 segundos en el ritmo se convierte en unos 21 minutos en una maratón. Cuanto más larga es la carrera,
      más se paga en la segunda mitad haber salido un poco rápido, así que, una vez fijado tu ritmo objetivo, no corras más rápido al principio.
    </p>

    <h2>Cómo fijar el ritmo según el objetivo</h2>
    <p>
      Correr siempre al mismo ritmo suele acumular cansancio sin mucha mejora. Dividir el entrenamiento por intensidades hace que
      el mismo tiempo invertido rinda mucho más. Las pautas siguientes son aproximadas y se basan en tu ritmo reciente de 5K.
    </p>
    <h3>Rodaje suave (la base de la mayor parte de tu running)</h3>
    <p>
      Un esfuerzo cómodo en el que puedes hablar con frases completas: aproximadamente 1 a 1,5 minutos por km más lento que tu ritmo de 5K.
      Si corres 5 km en 30 minutos (ritmo de 6:00), tu ritmo suave está en torno a 7:00–7:30. Debe parecer casi demasiado lento;
      esa es la intensidad correcta, y desarrolla la resistencia aeróbica y la capacidad de recuperación. Muchos enfoques de entrenamiento
      recomiendan dedicar alrededor del 80 % del tiempo de carrera a esta intensidad suave.
    </p>
    <h3>Tempo (cómodamente duro)</h3>
    <p>
      Un esfuerzo en el que puedes responder con frases cortas pero no mantener una conversación, sostenido durante 20–30 minutos.
      Apunta a unos 20–30 segundos por km más lento que tu ritmo de 5K. Mejora la capacidad de mantener una velocidad constante durante
      mucho tiempo, lo que ayuda en tus marcas de 10K y media maratón.
    </p>
    <h3>Intervalos (repeticiones cortas y rápidas)</h3>
    <p>
      Corre de 400 m a 1 km a tu ritmo de 5K o algo más rápido, recupera caminando o trotando suave el mismo tiempo y repite de 4 a 8 veces.
      Como son intensos, una vez por semana suele bastar, y quien empieza hace mejor en construir antes una base con rodajes suaves.
    </p>

    <h2>Por qué el ritmo del GPS salta</h2>
    <p>
      Seguramente has visto cómo tu ritmo actual salta de repente a 3 o 10 minutos por km en plena carrera. El GPS del móvil pierde
      precisión entre edificios altos, bajo pasos elevados y bajo árboles frondosos, así que el ritmo instantáneo en tramos cortos puede
      ser erróneo. <strong>Los parciales por kilómetro o el ritmo medio total</strong> son más fiables. Repetir el mismo recorrido también
      facilita las comparaciones, porque los errores propios del recorrido aparecen de forma similar cada vez.
    </p>

    <div class="guide-note">
      <p>
        <strong>En Pace League,</strong> tu ritmo medio se calcula automáticamente con la distancia y el tiempo del GPS al terminar,
        y una puntuación que refleja distancia y ritmo se suma a tu clasificación de temporada y a tu nivel. Mejorar el ritmo sube
        la puntuación para la misma distancia, así que construye la base con rodajes suaves y añade de vez en cuando tempo o intervalos.
      </p>
    </div>
"""

BODY['first-5k'] = """
    <p class="guide-lead">
      Aunque nunca hayas corrido 5 km sin parar, ocho semanas bastan para conseguirlo. La clave es no intentar correr sin pausa
      desde el primer día. Este plan alterna caminar y correr y aumenta poco a poco el tiempo de carrera, para que en la
      semana 8 puedas correr 30 minutos seguidos.
    </p>

    <h2>Tres cosas que conviene saber antes de empezar</h2>
    <ol>
      <li><strong>Olvídate de la velocidad.</strong> Corre tan despacio que aún puedas decir una frase corta, no tan fuerte que no puedas hablar. Al principio puede parecer poco más que caminar rápido, y está bien.</li>
      <li><strong>Entrena tres veces por semana con un día de descanso entre sesiones.</strong> Lunes/miércoles/viernes o martes/jueves/sábado funcionan bien. Músculos y articulaciones se adaptan y se fortalecen en los días de descanso.</li>
      <li><strong>Camina 5 minutos antes y después de cada sesión.</strong> Los tiempos de la tabla son solo del entrenamiento principal; añade 5 minutos de caminata rápida al principio y al final.</li>
    </ol>

    <h2>Plan semana a semana</h2>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Semana</th><th>Entrenamiento principal (3 por semana)</th><th>Tiempo total corriendo</th></tr>
        <tr><td>1</td><td>Correr 1 min + caminar 2 min × 8</td><td>8 min</td></tr>
        <tr><td>2</td><td>Correr 2 min + caminar 2 min × 6</td><td>12 min</td></tr>
        <tr><td>3</td><td>Correr 3 min + caminar 2 min × 5</td><td>15 min</td></tr>
        <tr><td>4</td><td>Correr 5 min + caminar 2 min × 4</td><td>20 min</td></tr>
        <tr><td>5</td><td>Correr 8 min + caminar 2 min × 3</td><td>24 min</td></tr>
        <tr><td>6</td><td>Correr 12 min + caminar 2 min × 2</td><td>24 min</td></tr>
        <tr><td>7</td><td>Correr 20 min seguidos (22 min en la 3.ª sesión)</td><td>20–22 min</td></tr>
        <tr><td>8</td><td>Correr 25 min seguidos → en la última sesión, intenta 5 km o 30 min</td><td>25–30 min</td></tr>
      </table>
    </div>
    <p>
      Si una semana te resulta demasiado dura, <strong>simplemente repítela</strong>. No pasa nada si las ocho semanas se convierten en diez o doce.
      En cambio, no es recomendable saltarse semanas porque algo parezca fácil. El corazón y los pulmones mejoran rápido, pero
      articulaciones y tendones se adaptan más despacio, y forzar solo porque respiras mejor suele acabar en dolor de rodilla o espinilla.
    </p>

    <h2>Claves de cada etapa</h2>
    <h3>Semanas 1–2: crear el hábito</h3>
    <p>
      El objetivo aquí no es la forma física sino <strong>el hábito de salir los días fijados</strong>. Si te quedas sin aliento en los
      tramos de 1–2 minutos, reduce aún más la velocidad. Los tramos caminando son parte del entrenamiento, no un descanso, así que sigue caminando sin detenerte.
    </p>
    <h3>Semanas 3–5: alargar los tramos de carrera</h3>
    <p>
      Cuando los tramos pasan a 3, 5 y 8 minutos, sentirás esfuerzo de verdad por primera vez. Respira por la nariz y la boca a la vez,
      relaja hombros y manos, y acorta la zancada para que los pies aterricen más cerca de tu cuerpo: se nota mucho.
    </p>
    <h3>Semanas 6–8: correr sin parar</h3>
    <p>
      En la semana 7 corres 20 minutos sin parar por primera vez. Es la mayor barrera mental, pero en las semanas 5–6 ya corriste
      dos veces 12 minutos (24 en total), así que la forma física está ahí. El secreto es empezar los primeros 10 minutos aún más lento de lo habitual.
      En la última sesión de la semana 8, intenta llegar a 5 km o a 30 minutos, lo que ocurra primero.
      Unos primeros 5 km en 35–40 minutos son un resultado excelente para un principiante.
    </p>

    <h2>Consejos para no abandonar</h2>
    <ul>
      <li><strong>Revisa primero tus zapatillas.</strong> Unas zapatillas de running con amortiguación cargan menos las articulaciones de un principiante que unas deportivas de suela fina. Pruébatelas en tienda y deja aproximadamente el ancho de un pulgar delante de los dedos.</li>
      <li><strong>Lleva un registro.</strong> Anotar la fecha, el tiempo corrido y cómo te sentiste te permite ver lo mucho más fácil que resulta todo tras 2–3 semanas: la mejor motivación.</li>
      <li><strong>No corras con dolor.</strong> Las agujetas son normales, pero si un punto duele de forma aguda o empiezas a cojear, para y descansa unos días. Si el dolor persiste, consulta a un médico.</li>
      <li><strong>Ten un plan B para el mal tiempo.</strong> Los días de lluvia o mala calidad del aire, la cinta o una caminata rápida mantienen el hábito.</li>
    </ul>

    <div class="guide-note">
      <p>
        <strong>En Pace League,</strong> al registrar tu carrera en la app se calculan automáticamente la distancia y el ritmo medio, que se
        guardan en tu historial, y tu puntuación de temporada y tu nivel suben a medida que corres. Observa tu evolución durante las ocho
        semanas y, el día que completes tus primeros 5 km, compártelo en el tablón de verificación de la comunidad con tu carrera adjunta.
      </p>
    </div>
"""

BODY['injury-prevention'] = """
    <p class="guide-lead">
      Correr no requiere equipamiento especial, pero como repite el mismo movimiento miles de veces, las lesiones por sobrecarga son frecuentes.
      Por suerte, muchas lesiones pueden reducirse simplemente cuidando la rapidez con la que aumentas el kilometraje y haciendo
      calentamiento y vuelta a la calma. Esta guía explica las causas de las lesiones más comunes y hábitos que puedes empezar hoy.
    </p>

    <div class="guide-note">
      <p>Este artículo ofrece información general sobre ejercicio y no sustituye un diagnóstico ni un tratamiento médico. Si el dolor persiste o hay hinchazón, consulta a un médico.</p>
    </div>

    <h2>Lesiones habituales en corredores</h2>
    <ul>
      <li><strong>Dolor en la parte delantera de la rodilla</strong>: suele notarse alrededor de la rótula o al bajar escaleras. Se conoce como «rodilla del corredor» y a menudo se relaciona con debilidad en muslos y caderas o con un aumento brusco del kilometraje.</li>
      <li><strong>Dolor en la espinilla</strong>: un dolor pulsátil a lo largo del borde interno de la tibia, frecuente al empezar a correr o al aumentar de golpe la distancia sobre superficies duras.</li>
      <li><strong>Dolor en la planta del pie</strong>: si la parte inferior del talón pica con los primeros pasos de la mañana, puede ser señal de sobrecarga de la fascia plantar.</li>
      <li><strong>Dolor en el tendón de Aquiles</strong>: rigidez y dolor en el tendón sobre el talón, que suele aparecer tras aumentar de repente las cuestas o el trabajo de velocidad.</li>
    </ul>
    <p>
      Lo que tienen en común es que <strong>la carga de entrenamiento aumentó más rápido de lo que el cuerpo podía adaptarse</strong>.
      La capacidad aeróbica mejora en semanas, pero huesos, tendones y ligamentos tardan meses en adaptarse. Aumentar distancia y velocidad
      a la vez solo porque respiras mejor abre precisamente esa brecha.
    </p>

    <h2>Aumenta la distancia despacio y de una en una</h2>
    <p>
      Una regla práctica muy extendida entre corredores es <strong>no aumentar la distancia semanal total más de un 10 % aproximadamente respecto a la semana anterior</strong>.
      Si esta semana corriste 20 km, unos 22 km la próxima es razonable. No es un umbral científico exacto, pero sirve bien para evitar
      saltos bruscos. Otros principios relacionados:
    </p>
    <ul>
      <li><strong>No subas distancia e intensidad a la vez.</strong> La semana en que añadas distancia, no añadas intervalos ni cuestas.</li>
      <li><strong>Cada 3 o 4 semanas, haz una semana más suave.</strong> Reduce la distancia semanal un 20–30 % para que el cuerpo se recupere.</li>
      <li><strong>Limita los días seguidos corriendo.</strong> A los principiantes les conviene intercalar un día de descanso o una caminata suave.</li>
    </ul>

    <h2>Rutina de calentamiento de 5–10 minutos</h2>
    <p>
      Acelerar con los músculos fríos aumenta el riesgo de lesión. Antes de correr, un calentamiento dinámico que mueva las articulaciones
      en todo su recorrido es más adecuado que los estiramientos estáticos (mantener una postura mucho tiempo).
    </p>
    <ol>
      <li>3–5 minutos de caminata rápida o trote muy suave</li>
      <li>Balanceos de pierna: apoyado en una pared, 10 hacia delante y atrás y 10 de lado a lado por pierna</li>
      <li>Zancadas caminando: 10 pasos largos</li>
      <li>Skipping y talones al glúteo: 20 segundos cada uno en el sitio</li>
      <li>Corre el primer kilómetro de la sesión más lento que tu objetivo</li>
    </ol>

    <h2>Vuelta a la calma y recuperación</h2>
    <p>
      En lugar de parar en seco al terminar, camina o trota suave 3–5 minutos para bajar las pulsaciones y luego estira de forma estática
      gemelos, parte delantera y trasera del muslo y glúteos durante 20–30 segundos cada uno. Dormir bien, hidratarse y tomar proteínas e
      hidratos en la comida posterior también ayudan a recuperarte para la próxima carrera.
    </p>

    <h2>10 minutos de fuerza, dos veces por semana</h2>
    <p>
      Solo corriendo, los músculos de caderas, muslos y gemelos que absorben el impacto no se fortalecen lo suficiente.
      Invertir 10 minutos los días sin carrera ayuda a reducir la carga sobre rodillas y tobillos.
    </p>
    <ul>
      <li>Sentadillas: 3 series de 15</li>
      <li>Puente de glúteos (tumbado, eleva la cadera): 3 series de 15</li>
      <li>Elevaciones de talones: 3 series de 20</li>
      <li>Plancha: 3 × 30 segundos</li>
    </ul>

    <h2>Cuándo cambiar las zapatillas</h2>
    <p>
      La amortiguación se desgasta poco a poco y sin que se note. Se suele citar como momento de cambio <strong>unos 500–800 km</strong>,
      y pueden gastarse antes según el peso y la técnica. Si la suela está muy gastada por un lado, o un dolor nuevo en las piernas desaparece
      al estrenar zapatillas, ya habías pasado el momento de cambiarlas. Anotar la distancia recorrida ayuda a calcularlo.
    </p>

    <h2>Cuándo debes descansar</h2>
    <ul>
      <li>Un punto concreto duele al presionarlo y el dolor empeora cuanto más corres</li>
      <li>Hay hinchazón o empiezas a cojear</li>
      <li>El dolor no remite tras varios días de descanso</li>
    </ul>
    <p>
      Unos días de descanso apenas afectan a tu forma, pero seguir corriendo con dolor hasta empeorar la lesión puede costarte semanas o meses.
    </p>

    <div class="guide-note">
      <p>
        <strong>En Pace League,</strong> tus carreras se registran por fecha, así que puedes comparar fácilmente la distancia total de esta semana con la anterior.
        Cuando aumentes distancia, revisa el incremento semanal en la pantalla de registros y planifica sin pasarte.
      </p>
    </div>
"""

BODY['landit-strategy'] = """
    <p class="guide-lead">
      Landeat es el juego de running de Pace League en el que tu ruta conquista terreno en un mapa real. Aunque corras los mismos 5 km,
      el área que consigues puede multiplicarse según la forma de tu ruta. Esta guía explica cuándo cuenta el terreno, cómo diseñar rutas
      que maximicen el área y cómo arrebatar terreno a otros corredores.
    </p>

    <h2>Cuándo cuenta el terreno</h2>
    <p>Empieza a correr con el modo Landeat activado; si se cumplen todas las condiciones siguientes, el terreno se crea en el momento en que terminas.</p>
    <ul>
      <li><strong>Debes volver al punto de partida.</strong> El punto final debe estar a menos de 50 m del inicio para que la ruta cuente como un circuito cerrado.</li>
      <li><strong>El perímetro debe ser de al menos 300 m.</strong> Los circuitos demasiado cortos no cuentan.</li>
      <li><strong>El área debe estar entre 10.000 m² (unos 100 m × 100 m) y 5 km².</strong> Se excluyen los circuitos demasiado pequeños o anormalmente grandes.</li>
    </ul>
    <p>
      Superadas estas comprobaciones, el área encerrada por tu circuito se convierte en <strong>casillas hexagonales</strong> en el mapa.
      No solo obtienes las casillas del interior, sino también todas las que atraviesa la ruta. Si amplías lo suficiente el mapa, puedes ver
      esta cuadrícula hexagonal y comprobar casilla a casilla dónde empieza y acaba el terreno de cada corredor.
    </p>

    <h2>Más área con la misma distancia: la forma lo es todo</h2>
    <p>
      Con el mismo perímetro, cuanto más se parezca la forma a un círculo, más área encierra. Por ejemplo, para un circuito de 1,2 km:
    </p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Forma del circuito (perímetro 1,2 km)</th><th>Área encerrada</th></tr>
        <tr><td>Rectángulo de 100 m × 500 m</td><td>unos 50.000 m²</td></tr>
        <tr><td>Cuadrado de 300 m × 300 m (una manzana)</td><td>unos 90.000 m²</td></tr>
        <tr><td>Círculo de unos 191 m de radio</td><td>unos 115.000 m²</td></tr>
      </table>
    </div>
    <p>
      Con los mismos 1,2 km, un circuito largo y estrecho consigue menos de la mitad del terreno que uno circular. En calles reales no se puede
      trazar un círculo perfecto, así que <strong>rodear una manzana de ancho y largo parecidos</strong> es la opción más práctica.
      Las rutas de ida y vuelta casi no encierran área y no generan terreno, así que evítalas.
    </p>
    <p>
      Ten en cuenta que una vuelta a una pista de atletismo de 400 m encierra solo unos 10.000 m², justo en el límite mínimo. Un pequeño error
      de GPS puede dejarla por debajo, así que el perímetro de un parque o una manzana residencial es una opción más segura que la pista.
    </p>

    <h2>Un circuito grande o varios pequeños</h2>
    <p>
      El área crece con el cuadrado del perímetro: si duplicas el perímetro, el área se multiplica aproximadamente por cuatro. Por eso un circuito
      de 3 km conquista mucho más terreno que tres de 1 km. Si tienes piernas, una vuelta grande por tu barrio es lo mejor para la clasificación por área.
      Un solo circuito de más de 5 km² no cuenta, pero eso equivale a un cuadrado de unos 2,2 km de lado, así que en carreras normales casi nunca ocurre.
    </p>

    <h2>Arrebatar terreno a otros corredores</h2>
    <p>
      Cuando tu nuevo circuito se solapa con el terreno de otra persona, <strong>las casillas hexagonales solapadas</strong> pasan a ser tuyas. La regla es sencilla:
      <strong>el último corredor que pasa por una casilla es su dueño</strong>. Da igual quién la conquistó primero o cuánto tiempo la ha tenido.
    </p>
    <ul>
      <li>No hace falta cubrir todo su territorio. Con un solapamiento parcial te llevas todas las casillas solapadas y su territorio se reduce a las que quedan.</li>
      <li>Si cubres todas las casillas del territorio de alguien, desaparece del mapa.</li>
      <li>Las casillas que ya son tuyas se mantienen al volver a pasar por ellas; solo las casillas vacías nuevas y las conquistadas se agrupan en tu nuevo terreno.</li>
    </ul>
    <p>
      La otra cara es que tu terreno también puede ser arrebatado en cualquier momento. Si hay otros corredores activos cerca de tus rutas habituales,
      repite el mismo circuito con regularidad para recuperar las casillas perdidas. La clasificación de Landeat en el mapa muestra el área total de
      cada corredor, así que ver dónde está el terreno de los primeros puestos también ayuda a planificar tus rutas.
    </p>

    <h2>Lista de comprobación para que tu carrera cuente</h2>
    <ol>
      <li>Antes de empezar, comprueba que el modo Landeat está activado.</li>
      <li>Recuerda el punto de partida y pulsa siempre finalizar a menos de 50 m de él.</li>
      <li>Evita circuitos en los que la mayor parte de la ruta pase por zonas de GPS débil, como entre edificios altos o por túneles.</li>
      <li>Elige una ruta que gire en un solo sentido en lugar de ida y vuelta.</li>
    </ol>

    <div class="guide-note">
      <p>
        <strong>La seguridad es lo primero.</strong> No cruces calzadas, no entres en propiedades privadas o zonas de obras restringidas ni corras
        de noche por lugares desiertos solo para conseguir más terreno. Todo el terreno puede conquistarse usando solo calles y aceras públicas.
      </p>
    </div>
"""
