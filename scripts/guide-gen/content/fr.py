# -*- coding: utf-8 -*-
"""Guide en français (traduit de ko.py)."""

UI = dict(crumb="Guide running", author="Équipe Pace League", date="30 septembre 2026", related="Autres guides", read_in="Lire en")

HUB = dict(title="Guide running",
           desc="Calcul de l'allure, plan 5 km en 8 semaines pour débutants, prévention des blessures et stratégie Landeat : les guides running de Pace League.",
           lead="Que vous fassiez vos premières foulées ou que vous cherchiez à battre votre record, ces guides rassemblent l'essentiel pour courir régulièrement et avec plaisir. Vous y trouverez aussi des conseils pour tirer le meilleur des classements et de Landeat sur Pace League.")

ARTICLES = {
  'pace': dict(tag='Bases', title="Comprendre l'allure de course : du calcul à l'allure selon l'intensité",
       desc="Ce que signifie l'allure (min/km), comment la calculer, la convertir en vitesse, tableaux de temps pour 5 km, 10 km, semi et marathon, et comment régler l'allure des footings, du tempo et du fractionné.",
       summary="Le lien entre allure et vitesse, un tableau des temps d'arrivée et l'allure à adopter en footing, tempo et fractionné."),
  'first-5k': dict(tag='Débutant', title='Plan de 8 semaines pour courir vos premiers 5 km',
       desc='Un plan semaine par semaine qui alterne marche et course pour que même les personnes peu sportives puissent courir 5 km sans s\'arrêter en 8 semaines, avec des conseils pratiques.',
       summary='Commencez par des intervalles marche-course et terminez 8 semaines plus tard en courant 5 km sans pause.'),
  'injury-prevention': dict(tag='Santé', title="Prévenir les blessures en course : échauffement, retour au calme et progression du kilométrage",
       desc="Causes et prévention des blessures courantes au genou, au tibia et au pied, routines d'échauffement et de retour au calme, augmentation sûre du kilométrage hebdomadaire et remplacement des chaussures.",
       summary="Les blessures fréquentes, l'échauffement et le retour au calme, et comment augmenter son kilométrage en sécurité."),
  'landit-strategy': dict(tag='Landeat', title='Stratégie Landeat : des parcours qui conquièrent plus de terrain pour la même distance',
       desc="Les règles de conquête de Landeat, le jeu de course de Pace League, comment concevoir des parcours qui couvrent plus de surface pour la même distance et comment prendre du terrain aux autres coureurs.",
       summary="Quand le terrain est validé, quelle forme de boucle maximise la surface et comment prendre du terrain aux autres."),
}

BODY = {}

BODY['pace'] = """
    <p class="guide-lead">
      L'allure est le premier chiffre qui s'affiche quand on ouvre une application de course. Mais il est souvent difficile de savoir
      si une « allure de 5'30 » est rapide ou lente, ou à quelle allure courir aujourd'hui. Ce guide explique ce qu'est l'allure,
      comment la calculer, comment transformer un temps visé en allure et comment la faire varier selon l'objectif de chaque sortie.
    </p>

    <h2>L'allure : le temps pour courir 1 km</h2>
    <p>
      En course à pied, l'allure désigne <strong>le temps nécessaire pour parcourir 1 km</strong>, généralement notée en minutes et secondes,
      par exemple <code>5'30"/km</code>. Les coureurs l'utilisent plutôt que la vitesse (km/h) parce qu'elle est plus intuitive : pour courir
      10 km en moins de 55 minutes, on sait tout de suite qu'il faut passer chaque kilomètre en 5 minutes 30 ou moins.
    </p>
    <p>
      Avec l'allure, <strong>plus le chiffre est petit, plus c'est rapide</strong>. Une allure de 5 minutes est une minute par kilomètre plus rapide
      qu'une allure de 6. Les débutants courent généralement entre 7 et 8 minutes au km, tandis que les coureurs amateurs réguliers
      courent souvent à l'aise autour de 5 minutes. L'allure varie beaucoup selon le poids, l'âge, le dénivelé et la météo : elle sert donc
      surtout à suivre <strong>l'évolution de vos propres performances</strong> plutôt qu'à vous comparer aux autres.
    </p>

    <h2>Calculer l'allure et la convertir en vitesse</h2>
    <p>L'allure se calcule ainsi : <strong>temps total ÷ distance (km)</strong>.</p>
    <ul>
      <li>5 km en 30 minutes : 30 ÷ 5 = <strong>6'00 au km</strong></li>
      <li>10 km en 58 minutes : 58 ÷ 10 = 5,8 minutes = <strong>5'48 au km</strong> (0,8 × 60 = 48 secondes)</li>
      <li>3,2 km en 20 minutes : 20 ÷ 3,2 = 6,25 minutes = <strong>6'15 au km</strong></li>
    </ul>
    <p>
      Vitesse et allure sont liées par <strong>allure (minutes) = 60 ÷ vitesse (km/h)</strong>. Un tapis réglé à 10 km/h correspond à
      60 ÷ 10 = une allure de 6 minutes ; 12 km/h, à une allure de 5 minutes. Pratique pour comparer le tapis et les sorties en extérieur.
    </p>

    <h2>Temps d'arrivée selon l'allure</h2>
    <p>Si vous visez un temps sur une course, retrouvez dans le tableau l'allure moyenne nécessaire. (Semi-marathon 21,0975 km, marathon 42,195 km)</p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Allure (/km)</th><th>Vitesse</th><th>5 km</th><th>10 km</th><th>Semi</th><th>Marathon</th></tr>
        <tr><td>4'30"</td><td>13,3 km/h</td><td>22:30</td><td>45:00</td><td>1:34:56</td><td>3:09:53</td></tr>
        <tr><td>5'00"</td><td>12,0 km/h</td><td>25:00</td><td>50:00</td><td>1:45:29</td><td>3:30:59</td></tr>
        <tr><td>5'30"</td><td>10,9 km/h</td><td>27:30</td><td>55:00</td><td>1:56:02</td><td>3:52:04</td></tr>
        <tr><td>6'00"</td><td>10,0 km/h</td><td>30:00</td><td>1:00:00</td><td>2:06:35</td><td>4:13:10</td></tr>
        <tr><td>6'30"</td><td>9,2 km/h</td><td>32:30</td><td>1:05:00</td><td>2:17:08</td><td>4:34:16</td></tr>
        <tr><td>7'00"</td><td>8,6 km/h</td><td>35:00</td><td>1:10:00</td><td>2:27:41</td><td>4:55:22</td></tr>
      </table>
    </div>
    <p>
      Une différence de 30 secondes d'allure devient environ 21 minutes sur un marathon. Plus la course est longue, plus un départ un peu
      trop rapide se paie en fin de parcours : une fois votre allure cible fixée, évitez de courir plus vite en début de course.
    </p>

    <h2>Régler l'allure selon l'objectif</h2>
    <p>
      Courir toujours à la même allure accumule la fatigue sans vraiment faire progresser. Répartir l'entraînement par intensité rend le même
      temps investi bien plus efficace. Les repères ci-dessous sont approximatifs et basés sur votre allure récente sur 5 km.
    </p>
    <h3>Footing facile (la base de la plupart de vos sorties)</h3>
    <p>
      Un effort confortable qui permet de parler en phrases complètes : environ 1 minute à 1 minute 30 par km de plus que votre allure 5 km.
      Si vous courez 5 km en 30 minutes (allure 6'00), votre allure de footing se situe vers 7'00–7'30. Cela doit sembler presque trop lent ;
      c'est la bonne intensité, qui développe l'endurance aérobie et la récupération. De nombreuses méthodes recommandent d'y consacrer
      environ 80 % du temps de course.
    </p>
    <h3>Tempo (confortablement difficile)</h3>
    <p>
      Un effort qui permet des réponses courtes mais pas une conversation, tenu 20 à 30 minutes. Visez environ 20 à 30 secondes par km
      de plus que votre allure 5 km. Il développe la capacité à maintenir une vitesse régulière longtemps, utile pour le 10 km et le semi.
    </p>
    <h3>Fractionné (répétitions courtes et rapides)</h3>
    <p>
      Courez 400 m à 1 km à votre allure 5 km ou un peu plus vite, récupérez en marchant ou en trottinant pendant la même durée, et répétez
      4 à 8 fois. C'est intense : une fois par semaine suffit généralement, et les débutants ont intérêt à construire d'abord une base en footing.
    </p>

    <h2>Pourquoi l'allure GPS fait des bonds</h2>
    <p>
      Vous avez sans doute vu votre allure instantanée passer soudain à 3 ou 10 minutes au km. Le GPS du téléphone perd en précision entre les
      immeubles, sous les ponts et sous les arbres denses : l'allure instantanée sur de courts tronçons peut donc être fausse.
      <strong>Les temps au kilomètre ou l'allure moyenne globale</strong> sont plus fiables. Courir souvent le même parcours facilite aussi les
      comparaisons, car les erreurs propres au parcours se répètent de façon similaire.
    </p>

    <div class="guide-note">
      <p>
        <strong>Sur Pace League,</strong> votre allure moyenne est calculée automatiquement à partir de la distance et du temps GPS à la fin
        de la sortie, et un score tenant compte de la distance et de l'allure s'ajoute à votre classement de saison et à votre rang.
        Améliorer votre allure augmente le score à distance égale : construisez votre base en footing et ajoutez de temps en temps du tempo ou du fractionné.
      </p>
    </div>
"""

BODY['first-5k'] = """
    <p class="guide-lead">
      Même si vous n'avez jamais couru 5 km sans vous arrêter, huit semaines suffisent pour y parvenir. La clé : ne pas chercher à courir
      en continu dès le premier jour. Ce plan alterne marche et course et allonge progressivement le temps de course, pour qu'en
      semaine 8 vous puissiez courir 30 minutes d'affilée.
    </p>

    <h2>Trois choses à savoir avant de commencer</h2>
    <ol>
      <li><strong>Oubliez la vitesse.</strong> Courez assez lentement pour pouvoir encore dire une courte phrase, pas au point de ne plus pouvoir parler. Au début, cela peut ressembler à de la marche rapide, et c'est très bien.</li>
      <li><strong>Trois séances par semaine, avec un jour de repos entre chaque.</strong> Lundi/mercredi/vendredi ou mardi/jeudi/samedi conviennent bien. Muscles et articulations s'adaptent et se renforcent pendant les jours de repos.</li>
      <li><strong>Marchez 5 minutes avant et après chaque séance.</strong> Les durées du tableau concernent la séance principale ; ajoutez 5 minutes de marche rapide au début et à la fin.</li>
    </ol>

    <h2>Plan semaine par semaine</h2>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Semaine</th><th>Séance principale (3 par semaine)</th><th>Temps de course total</th></tr>
        <tr><td>1</td><td>Courir 1 min + marcher 2 min × 8</td><td>8 min</td></tr>
        <tr><td>2</td><td>Courir 2 min + marcher 2 min × 6</td><td>12 min</td></tr>
        <tr><td>3</td><td>Courir 3 min + marcher 2 min × 5</td><td>15 min</td></tr>
        <tr><td>4</td><td>Courir 5 min + marcher 2 min × 4</td><td>20 min</td></tr>
        <tr><td>5</td><td>Courir 8 min + marcher 2 min × 3</td><td>24 min</td></tr>
        <tr><td>6</td><td>Courir 12 min + marcher 2 min × 2</td><td>24 min</td></tr>
        <tr><td>7</td><td>Courir 20 min en continu (22 min à la 3e séance)</td><td>20–22 min</td></tr>
        <tr><td>8</td><td>Courir 25 min en continu → à la dernière séance, tentez 5 km ou 30 min</td><td>25–30 min</td></tr>
      </table>
    </div>
    <p>
      Si une semaine est trop difficile, <strong>refaites-la simplement</strong>. Aucun problème si les huit semaines deviennent dix ou douze.
      À l'inverse, sauter des semaines parce que c'est facile n'est pas conseillé. Le cœur et les poumons progressent vite, mais les
      articulations et les tendons s'adaptent plus lentement : forcer parce que le souffle va mieux mène souvent à des douleurs au genou ou au tibia.
    </p>

    <h2>Les points clés de chaque étape</h2>
    <h3>Semaines 1–2 : prendre l'habitude</h3>
    <p>
      L'objectif ici n'est pas la condition physique mais <strong>l'habitude de sortir les jours prévus</strong>. Si vous êtes essoufflé pendant
      les 1–2 minutes de course, ralentissez encore. Les phases de marche font partie de la séance, ce n'est pas une pause : continuez à marcher.
    </p>
    <h3>Semaines 3–5 : allonger la course</h3>
    <p>
      Quand les phases de course passent à 3, 5 puis 8 minutes, vous ressentirez un vrai effort pour la première fois. Respirez par le nez et
      la bouche, relâchez épaules et mains, et raccourcissez la foulée pour poser le pied plus près sous le corps : cela change beaucoup.
    </p>
    <h3>Semaines 6–8 : courir en continu</h3>
    <p>
      En semaine 7, vous courez 20 minutes sans vous arrêter pour la première fois. C'est le plus gros obstacle mental, mais vous avez déjà
      couru deux fois 12 minutes (24 minutes au total) en semaines 5–6 : la condition est là. Le secret est de commencer les 10 premières
      minutes encore plus lentement que d'habitude. Lors de la dernière séance de la semaine 8, visez 5 km ou 30 minutes, selon ce qui arrive en premier.
      Un premier 5 km en 35 à 40 minutes est un excellent résultat pour un débutant.
    </p>

    <h2>Conseils pour tenir sur la durée</h2>
    <ul>
      <li><strong>Vérifiez d'abord vos chaussures.</strong> Des chaussures de course amorties sollicitent moins les articulations d'un débutant que des baskets à semelle fine. Essayez-les en magasin et gardez environ la largeur d'un pouce devant les orteils.</li>
      <li><strong>Gardez une trace.</strong> Noter la date, le temps couru et vos sensations permet de constater combien tout devient plus facile après 2 ou 3 semaines : la meilleure motivation qui soit.</li>
      <li><strong>Ne courez pas avec une douleur.</strong> Les courbatures sont normales, mais si un point fait mal de façon aiguë ou si vous boitez, arrêtez et reposez-vous quelques jours. Si la douleur persiste, consultez un médecin.</li>
      <li><strong>Prévoyez une solution de repli en cas de mauvais temps.</strong> Les jours de pluie ou de pollution, le tapis ou la marche rapide permettent de garder l'habitude.</li>
    </ul>

    <div class="guide-note">
      <p>
        <strong>Sur Pace League,</strong> enregistrer votre sortie dans l'application calcule automatiquement la distance et l'allure moyenne,
        ajoutées à votre historique, et votre score de saison et votre rang augmentent à mesure que vous courez. Suivez vos progrès pendant
        les huit semaines et, le jour où vous bouclez votre premier 5 km, partagez-le avec votre sortie jointe dans le forum de validation de la communauté.
      </p>
    </div>
"""

BODY['injury-prevention'] = """
    <p class="guide-lead">
      La course à pied ne demande aucun équipement particulier, mais comme elle répète le même geste des milliers de fois, les blessures de surmenage
      sont fréquentes. Heureusement, beaucoup peuvent être évitées simplement en surveillant la vitesse à laquelle on augmente le kilométrage et en
      soignant l'échauffement et le retour au calme. Ce guide présente les causes des blessures courantes et des habitudes à adopter dès aujourd'hui.
    </p>

    <div class="guide-note">
      <p>Cet article fournit des informations générales sur l'exercice et ne remplace pas un diagnostic ni un traitement médical. Si la douleur persiste ou s'il y a un gonflement, consultez un médecin.</p>
    </div>

    <h2>Les blessures fréquentes chez les coureurs</h2>
    <ul>
      <li><strong>Douleur à l'avant du genou</strong> : souvent une gêne autour de la rotule ou une douleur en descendant les escaliers. Souvent appelée « genou du coureur », elle est fréquemment liée à un manque de force des cuisses et des hanches ou à une hausse brutale du kilométrage.</li>
      <li><strong>Douleur au tibia</strong> : une douleur lancinante le long du bord interne du tibia, fréquente quand on débute ou qu'on augmente soudainement la distance sur sol dur.</li>
      <li><strong>Douleur sous le pied</strong> : si le dessous du talon pique dès les premiers pas le matin, cela peut signaler une surcharge de l'aponévrose plantaire.</li>
      <li><strong>Douleur au tendon d'Achille</strong> : raideur et douleur du tendon au-dessus du talon, qui apparaît souvent après une hausse soudaine des côtes ou du travail de vitesse.</li>
    </ul>
    <p>
      Leur point commun : <strong>la charge d'entraînement a augmenté plus vite que le corps ne pouvait s'adapter</strong>.
      La capacité aérobie progresse en quelques semaines, mais os, tendons et ligaments mettent des mois à s'adapter. Augmenter distance et vitesse
      en même temps parce que le souffle va mieux crée exactement cet écart.
    </p>

    <h2>Augmenter la distance lentement, une chose à la fois</h2>
    <p>
      Une règle empirique très répandue chez les coureurs consiste à <strong>n'augmenter la distance hebdomadaire totale que d'environ 10 % par rapport à la semaine précédente</strong>.
      Si vous avez couru 20 km cette semaine, environ 22 km la semaine suivante est raisonnable. Ce n'est pas un seuil scientifique précis, mais c'est
      un bon garde-fou contre les hausses brutales. Quelques principes associés :
    </p>
    <ul>
      <li><strong>N'augmentez pas distance et intensité en même temps.</strong> La semaine où vous ajoutez de la distance, n'ajoutez ni fractionné ni côtes.</li>
      <li><strong>Toutes les 3 ou 4 semaines, prévoyez une semaine allégée.</strong> Réduisez la distance hebdomadaire de 20 à 30 % pour laisser le corps récupérer.</li>
      <li><strong>Limitez les jours de course consécutifs.</strong> Les débutants gagnent à intercaler un jour de repos ou une marche tranquille entre deux sorties.</li>
    </ul>

    <h2>Un échauffement de 5 à 10 minutes</h2>
    <p>
      Accélérer à froid augmente le risque de blessure. Avant de courir, un échauffement dynamique qui mobilise les articulations sur toute leur amplitude
      convient mieux que les étirements statiques (tenir une position longtemps).
    </p>
    <ol>
      <li>3 à 5 minutes de marche rapide ou de trot très léger</li>
      <li>Balancements de jambe : appui contre un mur, 10 d'avant en arrière et 10 de côté par jambe</li>
      <li>Fentes marchées : 10 grands pas</li>
      <li>Montées de genoux et talons-fesses : 20 secondes chacun sur place</li>
      <li>Courez le premier kilomètre de la séance plus lentement que votre objectif</li>
    </ol>

    <h2>Retour au calme et récupération</h2>
    <p>
      Plutôt que de vous arrêter net, marchez ou trottinez 3 à 5 minutes pour faire baisser le rythme cardiaque, puis étirez de façon statique
      mollets, avant et arrière des cuisses et fessiers pendant 20 à 30 secondes chacun. Bien dormir, bien s'hydrater et prendre protéines et
      glucides au repas suivant aident aussi à récupérer pour la prochaine sortie.
    </p>

    <h2>10 minutes de renforcement, deux fois par semaine</h2>
    <p>
      Courir seul ne renforce pas suffisamment les muscles des hanches, des cuisses et des mollets qui absorbent les impacts.
      Consacrer 10 minutes les jours sans course aide à réduire les contraintes sur les genoux et les chevilles.
    </p>
    <ul>
      <li>Squats : 3 séries de 15</li>
      <li>Pont fessier (allongé, soulevez le bassin) : 3 séries de 15</li>
      <li>Montées sur la pointe des pieds : 3 séries de 20</li>
      <li>Gainage (planche) : 3 × 30 secondes</li>
    </ul>

    <h2>Quand changer de chaussures</h2>
    <p>
      L'amorti s'use progressivement et de façon invisible. On cite souvent <strong>environ 500 à 800 km</strong> comme moment de remplacement,
      et l'usure peut être plus rapide selon le poids et la foulée. Si la semelle est très usée d'un côté, ou si une douleur récente aux jambes
      disparaît avec une paire neuve, le moment de changer était passé. Noter votre kilométrage aide à l'estimer.
    </p>

    <h2>Quand il faut se reposer</h2>
    <ul>
      <li>Un point précis fait mal à la pression et la douleur augmente à mesure que vous courez</li>
      <li>Il y a un gonflement ou vous commencez à boiter</li>
      <li>La douleur ne diminue pas après plusieurs jours de repos</li>
    </ul>
    <p>
      Quelques jours de repos n'affectent presque pas votre forme, mais continuer à courir malgré la douleur jusqu'à aggraver la blessure peut coûter des semaines, voire des mois.
    </p>

    <div class="guide-note">
      <p>
        <strong>Sur Pace League,</strong> vos sorties sont enregistrées par date : vous pouvez facilement comparer la distance totale de cette semaine à celle de la précédente.
        Quand vous augmentez la distance, vérifiez la progression hebdomadaire dans l'écran des activités et planifiez sans en faire trop.
      </p>
    </div>
"""

BODY['landit-strategy'] = """
    <p class="guide-lead">
      Landeat est le jeu de course de Pace League dans lequel votre parcours conquiert du terrain sur une vraie carte. Même en courant les mêmes 5 km,
      la surface conquise peut varier de plusieurs fois selon la forme du parcours. Ce guide explique quand le terrain est validé, comment concevoir
      des parcours qui maximisent la surface et comment prendre du terrain aux autres coureurs.
    </p>

    <h2>Quand le terrain est validé</h2>
    <p>Lancez votre sortie avec le mode Landeat activé ; si toutes les conditions ci-dessous sont remplies, le terrain est créé dès la fin de la sortie.</p>
    <ul>
      <li><strong>Vous devez revenir à votre point de départ.</strong> Le point d'arrivée doit se trouver à moins de 50 m du départ pour que le parcours compte comme une boucle fermée.</li>
      <li><strong>Le périmètre doit faire au moins 300 m.</strong> Les boucles trop courtes ne comptent pas.</li>
      <li><strong>La surface doit être comprise entre 10 000 m² (environ 100 m × 100 m) et 5 km².</strong> Les boucles trop petites ou anormalement grandes sont exclues.</li>
    </ul>
    <p>
      Une fois ces vérifications passées, la zone entourée par votre boucle est convertie en <strong>tuiles hexagonales</strong> sur la carte. Vous obtenez
      non seulement les tuiles à l'intérieur du parcours, mais aussi toutes celles que le parcours traverse. En zoomant suffisamment, vous voyez cette grille
      hexagonale et pouvez vérifier tuile par tuile où commence et où s'arrête le terrain de chacun.
    </p>

    <h2>Plus de surface pour la même distance : tout est dans la forme</h2>
    <p>
      À périmètre égal, plus la forme se rapproche d'un cercle, plus elle entoure de surface. Par exemple, pour une boucle de 1,2 km :
    </p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Forme de la boucle (périmètre 1,2 km)</th><th>Surface entourée</th></tr>
        <tr><td>Rectangle de 100 m × 500 m</td><td>environ 50 000 m²</td></tr>
        <tr><td>Carré de 300 m × 300 m (un pâté de maisons)</td><td>environ 90 000 m²</td></tr>
        <tr><td>Cercle d'environ 191 m de rayon</td><td>environ 115 000 m²</td></tr>
      </table>
    </div>
    <p>
      Pour les mêmes 1,2 km, une boucle longue et étroite rapporte moins de la moitié du terrain d'une boucle circulaire. Dans la rue, impossible de courir
      un cercle parfait : <strong>faire le tour d'un pâté de maisons aux côtés à peu près égaux</strong> est le choix le plus réaliste.
      Les allers-retours n'entourent presque aucune surface et ne créent pas de terrain : évitez-les.
    </p>
    <p>
      À noter : un tour de piste d'athlétisme de 400 m n'entoure qu'environ 10 000 m², tout juste au seuil minimum. Une petite erreur GPS peut
      le faire passer en dessous : le tour d'un parc ou d'un pâté de maisons résidentiel est plus sûr qu'une piste.
    </p>

    <h2>Une grande boucle ou plusieurs petites</h2>
    <p>
      La surface croît avec le carré du périmètre : doublez le périmètre et la surface est multipliée par environ quatre. C'est pourquoi une boucle de 3 km
      conquiert bien plus de terrain que trois boucles de 1 km. Si vous en avez les jambes, un grand tour de votre quartier est idéal pour le classement par surface.
      Une boucle unique de plus de 5 km² n'est pas validée, mais cela correspond à un carré d'environ 2,2 km de côté : une sortie normale n'atteint presque jamais cette limite.
    </p>

    <h2>Prendre du terrain aux autres coureurs</h2>
    <p>
      Quand votre nouvelle boucle chevauche le terrain de quelqu'un d'autre, <strong>les tuiles hexagonales qui se chevauchent</strong> deviennent les vôtres. La règle est simple :
      <strong>le dernier coureur à passer sur une tuile en devient le propriétaire</strong>. Peu importe qui l'a conquise en premier ou depuis combien de temps.
    </p>
    <ul>
      <li>Inutile de couvrir tout son territoire. Même un chevauchement partiel vous donne toutes les tuiles concernées, et son territoire se réduit aux tuiles restantes.</li>
      <li>Si vous couvrez toutes les tuiles du territoire de quelqu'un, il disparaît de la carte.</li>
      <li>Les tuiles qui vous appartiennent déjà restent inchangées quand vous repassez dessus ; seules les nouvelles tuiles libres et les tuiles conquises forment votre nouveau terrain.</li>
    </ul>
    <p>
      Revers de la médaille : votre terrain peut lui aussi être pris à tout moment. Si d'autres coureurs sont actifs autour de vos parcours habituels,
      refaites régulièrement la même boucle pour reprendre les tuiles perdues. Le classement Landeat sur l'écran de la carte affiche la surface totale
      de chaque coureur : regarder où se trouve le terrain des mieux classés aide aussi à planifier vos parcours.
    </p>

    <h2>Check-list pour que votre sortie compte</h2>
    <ol>
      <li>Avant de partir, vérifiez que le mode Landeat est activé.</li>
      <li>Retenez votre point de départ et appuyez toujours sur « terminer » à moins de 50 m de celui-ci.</li>
      <li>Évitez les boucles dont la majeure partie passe par des zones où le GPS est faible, comme entre de grands immeubles ou dans des tunnels.</li>
      <li>Choisissez un parcours qui tourne dans un seul sens plutôt qu'un aller-retour.</li>
    </ol>

    <div class="guide-note">
      <p>
        <strong>La sécurité avant tout.</strong> Ne traversez pas la chaussée, n'entrez pas dans des propriétés privées ou des chantiers interdits d'accès et ne courez pas
        la nuit dans des lieux déserts juste pour gagner du terrain. Tout le terrain peut être conquis en utilisant uniquement les rues et trottoirs publics.
      </p>
    </div>
"""
