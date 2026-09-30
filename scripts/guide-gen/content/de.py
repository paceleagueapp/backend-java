# -*- coding: utf-8 -*-
"""Deutscher Ratgeber (übersetzt aus ko.py)."""

UI = dict(crumb="Lauf-Ratgeber", author="Pace-League-Team", date="30. September 2026", related="Weitere Ratgeber", read_in="Lesen auf")

HUB = dict(title="Lauf-Ratgeber",
           desc="Pace berechnen, 8-Wochen-Plan für die ersten 5 km, Verletzungen vorbeugen und Landeat-Strategie – Laufratgeber von Pace League.",
           lead="Ob du gerade mit dem Laufen anfängst oder deine Bestzeit verbessern willst: Hier findest du, was du brauchst, um regelmäßig und mit Freude zu laufen. Außerdem zeigen wir, wie du die Ranglisten und Landeat von Pace League besser nutzt.")

ARTICLES = {
  'pace': dict(tag='Grundlagen', title='Lauf-Pace verstehen: von der Berechnung bis zur Pace nach Trainingsintensität',
       desc='Was die Lauf-Pace (min/km) bedeutet, wie man sie berechnet und in Geschwindigkeit umrechnet, Zielzeittabellen für 5 km, 10 km, Halbmarathon und Marathon sowie die richtige Pace für lockere Läufe, Tempoläufe und Intervalle.',
       summary='Wie Pace und Geschwindigkeit zusammenhängen, eine Zielzeittabelle und die passende Pace für lockere Läufe, Tempoläufe und Intervalle.'),
  'first-5k': dict(tag='Einsteiger', title='In 8 Wochen zu den ersten 5 km: Plan für Laufanfänger',
       desc='Ein Wochenplan, der Gehen und Laufen kombiniert, damit auch Menschen ohne Sporterfahrung in 8 Wochen 5 km am Stück laufen können – mit praktischen Tipps.',
       summary='Mit Geh-Lauf-Intervallen starten und nach 8 Wochen 5 km ohne Pause laufen.'),
  'injury-prevention': dict(tag='Gesundheit', title='Laufverletzungen vorbeugen: Aufwärmen, Auslaufen und Umfang steigern',
       desc='Ursachen und Vorbeugung typischer Knie-, Schienbein- und Fußverletzungen, Routinen zum Aufwärmen und Auslaufen, sicheres Steigern des Wochenumfangs und wann die Laufschuhe ersetzt werden sollten.',
       summary='Typische Laufverletzungen, Aufwärm- und Auslaufroutinen und wie du deinen Wochenumfang sicher steigerst.'),
  'landit-strategy': dict(tag='Landeat', title='Landeat-Strategie: Routen, die bei gleicher Distanz mehr Land erobern',
       desc='Die Eroberungsregeln von Landeat, dem Laufspiel von Pace League, wie man Routen plant, die bei gleicher Distanz mehr Fläche einschließen, und wie man anderen Läufern Land abnimmt.',
       summary='Wann Land zählt, welche Schleifenform die Fläche maximiert und wie du anderen Läufern Land abnimmst.'),
}

BODY = {}

BODY['pace'] = """
    <p class="guide-lead">
      Die Pace ist die erste Zahl, die du siehst, wenn du eine Lauf-App öffnest. Oft ist aber unklar, ob eine „5:30er-Pace“ schnell oder
      langsam ist oder in welcher Pace du heute laufen solltest. Dieser Ratgeber erklärt, was die Pace bedeutet, wie man sie berechnet,
      wie man eine Zielzeit in eine Pace umrechnet und wie man die Pace je nach Trainingsziel variiert.
    </p>

    <h2>Pace: die Zeit für einen Kilometer</h2>
    <p>
      Beim Laufen bezeichnet die Pace <strong>die Zeit, die du für 1 km brauchst</strong>, meist in Minuten und Sekunden angegeben, etwa
      <code>5'30"/km</code>. Läufer nutzen die Pace statt der Geschwindigkeit (km/h), weil sie intuitiver ist: Wer 10 km unter 55 Minuten
      laufen will, weiß sofort, dass jeder Kilometer in höchstens 5 Minuten 30 Sekunden absolviert werden muss.
    </p>
    <p>
      Bei der Pace gilt: <strong>Je kleiner die Zahl, desto schneller</strong>. Eine 5er-Pace ist pro Kilometer eine Minute schneller als eine 6er-Pace.
      Einsteiger laufen meist im Bereich von 7–8 Minuten pro Kilometer, regelmäßig trainierende Freizeitläufer oft entspannt um die 5 Minuten.
      Die Pace hängt stark von Körpergewicht, Alter, Streckenprofil und Wetter ab – sie eignet sich daher eher dazu, <strong>die Entwicklung
      deiner eigenen Leistungen</strong> zu verfolgen, als dich mit anderen zu vergleichen.
    </p>

    <h2>Pace berechnen und in Geschwindigkeit umrechnen</h2>
    <p>Pace = <strong>Gesamtzeit ÷ Distanz (km)</strong>.</p>
    <ul>
      <li>5 km in 30 Minuten: 30 ÷ 5 = <strong>6:00 pro km</strong></li>
      <li>10 km in 58 Minuten: 58 ÷ 10 = 5,8 Minuten = <strong>5:48 pro km</strong> (0,8 × 60 = 48 Sekunden)</li>
      <li>3,2 km in 20 Minuten: 20 ÷ 3,2 = 6,25 Minuten = <strong>6:15 pro km</strong></li>
    </ul>
    <p>
      Geschwindigkeit und Pace hängen so zusammen: <strong>Pace (Minuten) = 60 ÷ Geschwindigkeit (km/h)</strong>. Ein Laufband mit 10 km/h
      entspricht 60 ÷ 10 = einer 6er-Pace, 12 km/h einer 5er-Pace. Praktisch, um Laufbandtraining mit Läufen im Freien zu vergleichen.
    </p>

    <h2>Zielzeiten nach Pace</h2>
    <p>Wenn du eine Zielzeit für einen Wettkampf hast, findest du in der Tabelle die nötige Durchschnittspace. (Halbmarathon 21,0975 km, Marathon 42,195 km)</p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Pace (/km)</th><th>Tempo</th><th>5 km</th><th>10 km</th><th>Halbmarathon</th><th>Marathon</th></tr>
        <tr><td>4'30"</td><td>13,3 km/h</td><td>22:30</td><td>45:00</td><td>1:34:56</td><td>3:09:53</td></tr>
        <tr><td>5'00"</td><td>12,0 km/h</td><td>25:00</td><td>50:00</td><td>1:45:29</td><td>3:30:59</td></tr>
        <tr><td>5'30"</td><td>10,9 km/h</td><td>27:30</td><td>55:00</td><td>1:56:02</td><td>3:52:04</td></tr>
        <tr><td>6'00"</td><td>10,0 km/h</td><td>30:00</td><td>1:00:00</td><td>2:06:35</td><td>4:13:10</td></tr>
        <tr><td>6'30"</td><td>9,2 km/h</td><td>32:30</td><td>1:05:00</td><td>2:17:08</td><td>4:34:16</td></tr>
        <tr><td>7'00"</td><td>8,6 km/h</td><td>35:00</td><td>1:10:00</td><td>2:27:41</td><td>4:55:22</td></tr>
      </table>
    </div>
    <p>
      30 Sekunden Unterschied in der Pace summieren sich im Marathon auf rund 21 Minuten. Je länger das Rennen, desto teurer wird ein etwas zu
      schneller Start in der zweiten Hälfte. Wenn du deine Zielpace festgelegt hast, lauf am Anfang also nicht schneller.
    </p>

    <h2>Pace nach Trainingsziel wählen</h2>
    <p>
      Wer jeden Lauf in derselben Pace absolviert, sammelt eher Müdigkeit als Fortschritt. Teilst du dein Training nach Intensität auf,
      bringt dieselbe investierte Zeit deutlich mehr. Die folgenden Richtwerte beziehen sich grob auf deine aktuelle 5-km-Pace.
    </p>
    <h3>Lockerer Dauerlauf (die Basis fast aller Läufe)</h3>
    <p>
      Eine angenehme Belastung, bei der du in ganzen Sätzen sprechen kannst – etwa 1 bis 1,5 Minuten pro Kilometer langsamer als deine 5-km-Pace.
      Läufst du 5 km in 30 Minuten (6:00er-Pace), liegt deine lockere Pace bei etwa 7:00–7:30. Es sollte sich fast zu langsam anfühlen – genau
      das ist die richtige Intensität, sie baut Grundlagenausdauer und Erholungsfähigkeit auf. Viele Trainingsansätze empfehlen, etwa 80 % der
      Laufzeit in diesem lockeren Bereich zu verbringen.
    </p>
    <h3>Tempolauf (angenehm anstrengend)</h3>
    <p>
      Eine Belastung, bei der kurze Antworten möglich sind, ein Gespräch aber nicht, gehalten über 20–30 Minuten. Ziel ist etwa 20–30 Sekunden
      pro Kilometer langsamer als deine 5-km-Pace. Tempoläufe trainieren die Fähigkeit, ein gleichmäßiges Tempo lange zu halten, und helfen bei 10 km und Halbmarathon.
    </p>
    <h3>Intervalle (kurz und schnell wiederholen)</h3>
    <p>
      Laufe 400 m bis 1 km in deiner 5-km-Pace oder etwas schneller, erhole dich gleich lang gehend oder locker trabend und wiederhole das 4- bis 8-mal.
      Da Intervalle intensiv sind, reicht meist einmal pro Woche; Einsteiger bauen besser zuerst mit lockeren Läufen eine Grundlage auf.
    </p>

    <h2>Warum die GPS-Pace springt</h2>
    <p>
      Sicher hast du schon erlebt, dass deine aktuelle Pace mitten im Lauf plötzlich auf 3 oder 10 Minuten pro Kilometer springt. Das GPS im Smartphone
      wird zwischen hohen Gebäuden, unter Brücken und unter dichten Bäumen ungenauer, sodass die momentane Pace auf kurzen Abschnitten falsch sein kann.
      <strong>Kilometerzeiten oder die Durchschnittspace</strong> sind verlässlicher. Wer dieselbe Strecke regelmäßig läuft, kann Ergebnisse zudem
      leichter vergleichen, weil streckenbedingte Fehler jedes Mal ähnlich auftreten.
    </p>

    <div class="guide-note">
      <p>
        <strong>Bei Pace League</strong> wird deine Durchschnittspace nach dem Lauf automatisch aus GPS-Distanz und Zeit berechnet, und ein Punktwert,
        der Distanz und Pace berücksichtigt, fließt in deine Saisonrangliste und deine Stufe ein. Eine bessere Pace bringt bei gleicher Distanz mehr Punkte –
        bau also mit lockeren Läufen eine Basis auf und streue ab und zu Tempoläufe oder Intervalle ein.
      </p>
    </div>
"""

BODY['first-5k'] = """
    <p class="guide-lead">
      Auch wenn du noch nie 5 km am Stück gelaufen bist: Acht Wochen reichen, um das zu schaffen. Der Schlüssel ist, nicht vom ersten Tag an
      durchlaufen zu wollen. Dieser Plan wechselt zwischen Gehen und Laufen und verlängert die Laufzeit schrittweise, sodass du in Woche 8
      30 Minuten am Stück laufen kannst.
    </p>

    <h2>Drei Dinge, die du vor dem Start wissen solltest</h2>
    <ol>
      <li><strong>Vergiss das Tempo.</strong> Lauf so langsam, dass du noch einen kurzen Satz sagen könntest – nicht so schnell, dass du nicht mehr sprechen kannst. Anfangs fühlt es sich vielleicht kaum schneller an als zügiges Gehen, und das ist völlig in Ordnung.</li>
      <li><strong>Dreimal pro Woche, mit einem Ruhetag dazwischen.</strong> Mo/Mi/Fr oder Di/Do/Sa funktionieren gut. Muskeln und Gelenke passen sich an den Ruhetagen an und werden stärker.</li>
      <li><strong>Geh vor und nach jeder Einheit 5 Minuten.</strong> Die Zeiten in der Tabelle gelten nur für das Hauptprogramm; ergänze vorn und hinten je 5 Minuten zügiges Gehen.</li>
    </ol>

    <h2>Trainingsplan Woche für Woche</h2>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Woche</th><th>Hauptprogramm (3× pro Woche)</th><th>Laufzeit gesamt</th></tr>
        <tr><td>1</td><td>1 Min. laufen + 2 Min. gehen × 8</td><td>8 Min.</td></tr>
        <tr><td>2</td><td>2 Min. laufen + 2 Min. gehen × 6</td><td>12 Min.</td></tr>
        <tr><td>3</td><td>3 Min. laufen + 2 Min. gehen × 5</td><td>15 Min.</td></tr>
        <tr><td>4</td><td>5 Min. laufen + 2 Min. gehen × 4</td><td>20 Min.</td></tr>
        <tr><td>5</td><td>8 Min. laufen + 2 Min. gehen × 3</td><td>24 Min.</td></tr>
        <tr><td>6</td><td>12 Min. laufen + 2 Min. gehen × 2</td><td>24 Min.</td></tr>
        <tr><td>7</td><td>20 Min. am Stück laufen (3. Einheit: 22 Min.)</td><td>20–22 Min.</td></tr>
        <tr><td>8</td><td>25 Min. am Stück → in der letzten Einheit 5 km oder 30 Min. versuchen</td><td>25–30 Min.</td></tr>
      </table>
    </div>
    <p>
      Ist eine Woche zu schwer, <strong>wiederhole sie einfach</strong>. Es ist völlig in Ordnung, wenn aus acht Wochen zehn oder zwölf werden.
      Wochen zu überspringen, weil es leicht erscheint, ist dagegen nicht empfehlenswert. Herz und Lunge werden schnell besser, Gelenke und Sehnen
      passen sich aber langsamer an – wer nur wegen der leichteren Atmung mehr macht, bekommt oft Knie- oder Schienbeinschmerzen.
    </p>

    <h2>Worauf es in jeder Phase ankommt</h2>
    <h3>Woche 1–2: Gewohnheit aufbauen</h3>
    <p>
      Ziel ist hier nicht Fitness, sondern <strong>die Gewohnheit, an festen Tagen vor die Tür zu gehen</strong>. Wenn du bei den 1–2 Minuten Laufen
      außer Atem kommst, werde noch langsamer. Die Gehpausen sind Teil des Trainings, keine Pause – also weitergehen statt stehen bleiben.
    </p>
    <h3>Woche 3–5: Laufabschnitte verlängern</h3>
    <p>
      Wenn die Laufabschnitte auf 3, 5 und 8 Minuten wachsen, spürst du zum ersten Mal echte Anstrengung. Atme durch Nase und Mund, entspanne
      Schultern und Hände und verkürze die Schritte, damit die Füße näher unter dem Körper aufsetzen – das macht einen großen Unterschied.
    </p>
    <h3>Woche 6–8: am Stück laufen</h3>
    <p>
      In Woche 7 läufst du zum ersten Mal 20 Minuten ohne Pause. Das ist die größte mentale Hürde, aber in Woche 5–6 bist du bereits zweimal
      12 Minuten gelaufen (insgesamt 24 Minuten) – die Fitness ist also da. Das Geheimnis: Die ersten 10 Minuten noch langsamer beginnen als sonst.
      In der letzten Einheit von Woche 8 läufst du bis 5 km oder 30 Minuten – je nachdem, was zuerst erreicht ist.
      35–40 Minuten für die ersten 5 km sind für Einsteiger ein hervorragendes Ergebnis.
    </p>

    <h2>Tipps zum Dranbleiben</h2>
    <ul>
      <li><strong>Prüfe zuerst deine Schuhe.</strong> Gedämpfte Laufschuhe belasten die Gelenke von Einsteigern weniger als dünnsohlige Alltagssneaker. Probiere sie im Geschäft an und lass vor den Zehen etwa eine Daumenbreite Platz.</li>
      <li><strong>Führe ein Protokoll.</strong> Wer Datum, Laufzeit und Befinden notiert, sieht nach 2–3 Wochen, wie viel leichter alles geworden ist – die beste Motivation überhaupt.</li>
      <li><strong>Lauf nicht mit Schmerzen.</strong> Muskelkater ist normal, aber wenn eine Stelle stechend schmerzt oder du zu hinken beginnst, hör auf und ruh dich ein paar Tage aus. Halten die Schmerzen an, geh zum Arzt.</li>
      <li><strong>Plane eine Alternative für schlechtes Wetter.</strong> An Tagen mit Regen oder schlechter Luft halten Laufband oder zügiges Gehen die Gewohnheit aufrecht.</li>
    </ul>

    <div class="guide-note">
      <p>
        <strong>Bei Pace League</strong> werden Distanz und Durchschnittspace automatisch berechnet und in deiner Historie gespeichert, wenn du deinen Lauf
        in der App aufzeichnest, und deine Saisonpunkte und Stufe steigen mit jedem Lauf. Verfolge deine Fortschritte über die acht Wochen – und an dem Tag,
        an dem du deine ersten 5 km schaffst, teile es mit angehängtem Lauf im Nachweis-Board der Community.
      </p>
    </div>
"""

BODY['injury-prevention'] = """
    <p class="guide-lead">
      Laufen braucht keine besondere Ausrüstung, doch weil dieselbe Bewegung tausendfach wiederholt wird, sind Überlastungsverletzungen häufig.
      Zum Glück lassen sich viele Laufverletzungen schon dadurch reduzieren, dass man auf das Steigerungstempo beim Umfang achtet und sich
      auf- und ausläuft. Dieser Ratgeber erklärt die Ursachen typischer Verletzungen und Gewohnheiten, mit denen du heute beginnen kannst.
    </p>

    <div class="guide-note">
      <p>Dieser Artikel enthält allgemeine Trainingsinformationen und ersetzt keine ärztliche Diagnose oder Behandlung. Bei anhaltenden Schmerzen oder Schwellungen wende dich an einen Arzt.</p>
    </div>

    <h2>Typische Verletzungen bei Läufern</h2>
    <ul>
      <li><strong>Schmerzen vorn am Knie</strong>: oft ein Druckgefühl rund um die Kniescheibe oder Schmerzen beim Treppabgehen. Häufig „Läuferknie“ genannt und oft mit schwacher Oberschenkel- und Hüftmuskulatur oder einem plötzlichen Umfangssprung verbunden.</li>
      <li><strong>Schienbeinschmerzen</strong>: ein pochender Schmerz entlang der Innenkante des Schienbeins, typisch für Laufanfänger oder bei plötzlich mehr Distanz auf hartem Untergrund.</li>
      <li><strong>Schmerzen an der Fußsohle</strong>: Sticht die Unterseite der Ferse bei den ersten Schritten am Morgen, kann das auf eine Überlastung der Plantarfaszie hindeuten.</li>
      <li><strong>Achillessehnenschmerzen</strong>: Steifheit und Schmerz in der Sehne oberhalb der Ferse, oft nach einer plötzlichen Steigerung von Berg- oder Tempotraining.</li>
    </ul>
    <p>
      Allen gemeinsam ist: <strong>Die Trainingsbelastung ist schneller gestiegen, als sich der Körper anpassen konnte</strong>.
      Die Ausdauer verbessert sich in wenigen Wochen, Knochen, Sehnen und Bänder brauchen dafür aber Monate. Wer Distanz und Tempo gleichzeitig
      steigert, nur weil die Atmung leichter fällt, öffnet genau diese Lücke.
    </p>

    <h2>Distanz langsam steigern – immer nur eine Sache</h2>
    <p>
      Eine unter Läufern verbreitete Faustregel lautet, <strong>den Wochenumfang gegenüber der Vorwoche nur um etwa 10 % zu erhöhen</strong>.
      Bist du diese Woche 20 km gelaufen, sind nächste Woche rund 22 km angemessen. Das ist kein exakter wissenschaftlicher Grenzwert, aber ein
      guter Schutz vor sprunghaften Steigerungen. Weitere Grundsätze:
    </p>
    <ul>
      <li><strong>Distanz und Intensität nicht gleichzeitig steigern.</strong> In einer Woche mit mehr Distanz keine zusätzlichen Intervalle oder Bergläufe.</li>
      <li><strong>Alle 3–4 Wochen eine leichtere Woche einplanen.</strong> Den Wochenumfang um 20–30 % senken, damit sich der Körper erholen kann.</li>
      <li><strong>Weniger Lauftage hintereinander.</strong> Einsteiger legen zwischen zwei Läufe am besten einen Ruhetag oder einen lockeren Spaziergang.</li>
    </ul>

    <h2>5–10 Minuten Aufwärmen</h2>
    <p>
      Mit kalten Muskeln Tempo zu machen, erhöht das Verletzungsrisiko. Vor dem Laufen eignet sich ein dynamisches Aufwärmen, das die Gelenke durch
      ihren Bewegungsumfang führt, besser als statisches Dehnen (eine Position lange halten).
    </p>
    <ol>
      <li>3–5 Minuten zügiges Gehen oder sehr lockeres Traben</li>
      <li>Beinschwingen: an einer Wand abstützen, pro Bein 10-mal vor und zurück und 10-mal seitlich</li>
      <li>Ausfallschritte im Gehen: 10 große Schritte</li>
      <li>Kniehebeläufe und Anfersen: je 20 Sekunden auf der Stelle</li>
      <li>Den ersten Kilometer des Hauptlaufs langsamer als geplant laufen</li>
    </ol>

    <h2>Auslaufen und Erholung</h2>
    <p>
      Statt nach dem Lauf abrupt stehen zu bleiben, 3–5 Minuten gehen oder locker traben, um den Puls zu senken, und dann Waden, Oberschenkelvorder-
      und -rückseite sowie Gesäß je 20–30 Sekunden statisch dehnen. Guter Schlaf, ausreichend Flüssigkeit sowie Eiweiß und Kohlenhydrate in der
      Mahlzeit danach helfen ebenfalls, dich für den nächsten Lauf zu erholen.
    </p>

    <h2>Zweimal pro Woche 10 Minuten Krafttraining</h2>
    <p>
      Laufen allein macht die Hüft-, Oberschenkel- und Wadenmuskeln, die die Landekräfte abfangen, nicht stark genug.
      Schon 10 Minuten an lauffreien Tagen helfen, Knie und Sprunggelenke zu entlasten.
    </p>
    <ul>
      <li>Kniebeugen: 3 × 15</li>
      <li>Glute Bridge (in Rückenlage das Becken anheben): 3 × 15</li>
      <li>Wadenheben: 3 × 20</li>
      <li>Unterarmstütz (Plank): 3 × 30 Sekunden</li>
    </ul>

    <h2>Wann Laufschuhe ersetzt werden sollten</h2>
    <p>
      Die Dämpfung eines Laufschuhs lässt allmählich und unbemerkt nach. Häufig werden <strong>etwa 500–800 km</strong> als Richtwert für den Austausch
      genannt; je nach Körpergewicht und Laufstil kann der Verschleiß schneller gehen. Ist die Sohle einseitig stark abgelaufen oder verschwinden neue
      Beinschmerzen mit einem neuen Paar, war der Zeitpunkt schon überschritten. Wer seine Kilometer notiert, kann ihn leichter abschätzen.
    </p>

    <h2>Wann du pausieren solltest</h2>
    <ul>
      <li>Eine bestimmte Stelle schmerzt auf Druck und der Schmerz wird beim Laufen stärker</li>
      <li>Es gibt eine Schwellung oder du beginnst zu hinken</li>
      <li>Der Schmerz lässt auch nach mehreren Ruhetagen nicht nach</li>
    </ul>
    <p>
      Ein paar Tage Pause wirken sich kaum auf deine Form aus – wer dagegen mit Schmerzen weiterläuft, bis die Verletzung schlimmer wird, fällt womöglich Wochen oder Monate aus.
    </p>

    <div class="guide-note">
      <p>
        <strong>Bei Pace League</strong> werden deine Läufe nach Datum gespeichert, sodass du den Gesamtumfang dieser Woche leicht mit dem der Vorwoche vergleichen kannst.
        Wenn du die Distanz steigerst, prüfe die wöchentliche Zunahme im Verlauf und plane so, dass du es nicht übertreibst.
      </p>
    </div>
"""

BODY['landit-strategy'] = """
    <p class="guide-lead">
      Landeat ist das Laufspiel von Pace League, bei dem deine Route auf einer echten Karte Land erobert. Selbst bei denselben 5 km kann die eroberte
      Fläche je nach Form der Route um ein Vielfaches variieren. Dieser Ratgeber erklärt, wann Land zählt, wie du Routen mit maximaler Fläche planst
      und wie du anderen Läufern Land abnimmst.
    </p>

    <h2>Wann Land zählt</h2>
    <p>Starte deinen Lauf mit aktiviertem Landeat-Modus; sind alle folgenden Bedingungen erfüllt, entsteht das Land in dem Moment, in dem dein Lauf endet.</p>
    <ul>
      <li><strong>Du musst zum Startpunkt zurückkehren.</strong> Der Endpunkt muss weniger als 50 m vom Start entfernt sein, damit die Route als geschlossene Schleife zählt.</li>
      <li><strong>Der Umfang muss mindestens 300 m betragen.</strong> Zu kurze Schleifen zählen nicht.</li>
      <li><strong>Die Fläche muss zwischen 10.000 m² (etwa 100 m × 100 m) und 5 km² liegen.</strong> Zu kleine oder unrealistisch große Schleifen werden ausgeschlossen.</li>
    </ul>
    <p>
      Sind diese Prüfungen bestanden, wird die von deiner Schleife umschlossene Fläche in <strong>sechseckige Felder</strong> auf der Karte umgewandelt.
      Du erhältst nicht nur die Felder innerhalb der Route, sondern auch alle, die die Route berührt. Zoomst du weit genug in die Karte, siehst du dieses
      Sechseckraster selbst und kannst Feld für Feld prüfen, wo das Land der einzelnen Läufer beginnt und endet.
    </p>

    <h2>Mehr Fläche bei gleicher Distanz: Die Form entscheidet</h2>
    <p>
      Bei gleichem Umfang schließt eine Form umso mehr Fläche ein, je näher sie einem Kreis kommt. Beispiel für eine Schleife mit 1,2 km Umfang:
    </p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Schleifenform (Umfang 1,2 km)</th><th>Eingeschlossene Fläche</th></tr>
        <tr><td>Rechteck 100 m × 500 m</td><td>etwa 50.000 m²</td></tr>
        <tr><td>Quadrat 300 m × 300 m (ein Häuserblock)</td><td>etwa 90.000 m²</td></tr>
        <tr><td>Kreis mit rund 191 m Radius</td><td>etwa 115.000 m²</td></tr>
      </table>
    </div>
    <p>
      Bei denselben 1,2 km bringt eine lange, schmale Schleife nicht einmal halb so viel Land wie eine kreisförmige. Auf echten Straßen lässt sich kein
      perfekter Kreis laufen – <strong>einen Block mit ähnlicher Länge und Breite zu umrunden</strong> ist daher die praktischste Wahl.
      Hin-und-zurück-Strecken schließen fast keine Fläche ein und erzeugen kein Land – vermeide sie.
    </p>
    <p>
      Übrigens: Eine Runde auf einer 400-m-Leichtathletikbahn umschließt nur etwa 10.000 m² – genau an der Mindestgrenze. Schon ein kleiner GPS-Fehler
      kann darunter fallen, daher ist ein Parkrand oder ein Wohnblock sicherer als die Bahn.
    </p>

    <h2>Eine große Schleife oder mehrere kleine?</h2>
    <p>
      Die Fläche wächst mit dem Quadrat des Umfangs: Doppelter Umfang bedeutet ungefähr vierfache Fläche. Deshalb erobert eine 3-km-Schleife weit mehr
      Land als drei 1-km-Schleifen. Wenn du die Kondition hast, ist eine große Runde durch dein Viertel ideal für die Flächenrangliste.
      Eine einzelne Schleife über 5 km² zählt nicht, doch das entspricht einem Quadrat mit rund 2,2 km Seitenlänge – bei normalen Läufen kommt das kaum vor.
    </p>

    <h2>Anderen Läufern Land abnehmen</h2>
    <p>
      Überschneidet sich deine neue Schleife mit dem Land eines anderen, gehören <strong>die überlappenden Sechseckfelder</strong> dir. Die Regel ist einfach:
      <strong>Wer zuletzt über ein Feld läuft, dem gehört es</strong>. Wer es zuerst erobert hat oder wie lange er es besitzt, spielt keine Rolle.
    </p>
    <ul>
      <li>Du musst nicht das ganze Gebiet abdecken. Schon eine teilweise Überschneidung bringt dir alle überlappenden Felder, und das andere Gebiet schrumpft auf die verbleibenden Felder.</li>
      <li>Deckst du alle Felder eines Gebiets ab, verschwindet es von der Karte.</li>
      <li>Felder, die dir schon gehören, bleiben beim erneuten Überlaufen unverändert; nur neu abgedeckte freie Felder und eroberte Felder werden zu deinem neuen Land zusammengefasst.</li>
    </ul>
    <p>
      Die Kehrseite: Auch dein Land kann dir jederzeit abgenommen werden. Sind rund um deine üblichen Strecken andere Läufer aktiv, lauf dieselbe Schleife
      regelmäßig erneut, um verlorene Felder zurückzuholen. Die Landeat-Rangliste auf der Kartenansicht zeigt die Gesamtfläche jedes Läufers – es hilft
      also auch, sich anzusehen, wo das Land der Spitzenläufer liegt, und die Routen danach zu planen.
    </p>

    <h2>Checkliste, damit dein Lauf zählt</h2>
    <ol>
      <li>Prüfe vor dem Start, ob der Landeat-Modus aktiviert ist.</li>
      <li>Merke dir den Startpunkt und tippe auf „Beenden“ immer innerhalb von 50 m davon.</li>
      <li>Vermeide Schleifen, deren Großteil durch Bereiche mit schwachem GPS führt, etwa zwischen Hochhäusern oder durch Tunnel.</li>
      <li>Wähle eine Route, die in eine Richtung verläuft, statt hin und zurück.</li>
    </ol>

    <div class="guide-note">
      <p>
        <strong>Sicherheit geht vor.</strong> Überquere nicht einfach Fahrbahnen, betritt keine gesperrten Privatgrundstücke oder Baustellen und lauf nicht nachts an
        menschenleeren Orten, nur um mehr Land zu erobern. Jedes Stück Land lässt sich allein über öffentliche Straßen und Gehwege erobern.
      </p>
    </div>
"""
