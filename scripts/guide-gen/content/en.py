# -*- coding: utf-8 -*-
"""English guide (translated from ko.py)."""

UI = dict(crumb="Running Guide", author="Pace League Team", date="September 30, 2026", related="More guides", read_in="Read in")

HUB = dict(title="Running Guide",
           desc="Pace calculation, an 8-week beginner 5K plan, injury prevention and Landeat strategy — running guides from Pace League.",
           lead="Whether you are taking your very first steps or trying to shave time off your personal best, these guides cover what you need to keep running consistently and enjoyably. You will also find tips for getting more out of Pace League's rankings and Landeat.")

ARTICLES = {
  'pace': dict(tag='Basics', title='Understanding Running Pace: From Calculation to Pace by Training Intensity',
       desc='What running pace (min/km) means, how to calculate it, how it converts to speed, finish-time tables for 5K, 10K, half and full marathon, and how to set easy, tempo and interval paces.',
       summary='How pace relates to speed, a finish-time conversion table, and which pace to use for easy runs, tempo runs and intervals.'),
  'first-5k': dict(tag='Beginner', title='An 8-Week 5K Plan for New Runners',
       desc='A week-by-week plan that mixes walking and running so that even people who rarely exercise can run 5 km without stopping in 8 weeks, plus practical tips.',
       summary='Start with walk–run intervals and finish 8 weeks later running 5 km without a break.'),
  'injury-prevention': dict(tag='Health', title='Preventing Running Injuries: Warm-Up, Cool-Down and Building Mileage',
       desc='Causes and prevention of common knee, shin and foot injuries in runners, warm-up and cool-down routines, how to increase weekly mileage safely, and when to replace your shoes.',
       summary='Common running injuries, warm-up and cool-down routines, and how to build weekly mileage safely.'),
  'landit-strategy': dict(tag='Landeat', title='Landeat Strategy: Routes That Claim More Land for the Same Distance',
       desc="The capture rules of Pace League's Landeat running game, how to design routes that claim more land for the same distance, and how to take land from other runners.",
       summary='When land counts, which loop shapes maximize area, and how to take land from other runners.'),
}

BODY = {}

BODY['pace'] = """
    <p class="guide-lead">
      Pace is the first number you see when you open a running app. But it is often hard to tell whether a "5:30 pace"
      is fast or slow, or what pace you should run today. This guide explains what pace means, how to calculate it,
      how to turn a target finish time into a pace, and how to vary your pace depending on the purpose of each run.
    </p>

    <h2>Pace: the time it takes to run 1 km</h2>
    <p>
      In running, pace means <strong>the time it takes to cover 1 km</strong>, usually written in minutes and seconds such as
      <code>5'30"/km</code>. Runners use pace instead of speed (km/h) because it is more intuitive: if you want to run
      10 km in under 55 minutes, you immediately know you need to pass each kilometer in 5 minutes 30 seconds or less.
    </p>
    <p>
      With pace, <strong>smaller numbers are faster</strong>. A 5-minute pace is one minute per kilometer faster than a 6-minute pace.
      People who are just starting out typically run at 7–8 minutes per km, while recreational runners who train regularly
      often run comfortably at around 5 minutes. Pace varies a lot with body weight, age, course gradient and weather,
      so it is more useful for tracking <strong>changes in your own running</strong> than for comparing yourself with others.
    </p>

    <h2>Calculating pace and converting to speed</h2>
    <p>Pace is <strong>total running time ÷ distance (km)</strong>.</p>
    <ul>
      <li>5 km in 30 minutes: 30 ÷ 5 = <strong>6:00 per km</strong></li>
      <li>10 km in 58 minutes: 58 ÷ 10 = 5.8 minutes = <strong>5:48 per km</strong> (0.8 × 60 = 48 seconds)</li>
      <li>3.2 km in 20 minutes: 20 ÷ 3.2 = 6.25 minutes = <strong>6:15 per km</strong></li>
    </ul>
    <p>
      Speed and pace are related by <strong>pace (minutes) = 60 ÷ speed (km/h)</strong>. A treadmill set to 10 km/h means
      60 ÷ 10 = a 6-minute pace; 12 km/h means a 5-minute pace. This is handy when comparing treadmill sessions with outdoor runs.
    </p>

    <h2>Finish times by pace</h2>
    <p>If you have a target race time, use the table below to find the average pace you need. (Half marathon 21.0975 km, marathon 42.195 km)</p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Pace (/km)</th><th>Speed</th><th>5K</th><th>10K</th><th>Half</th><th>Marathon</th></tr>
        <tr><td>4'30"</td><td>13.3 km/h</td><td>22:30</td><td>45:00</td><td>1:34:56</td><td>3:09:53</td></tr>
        <tr><td>5'00"</td><td>12.0 km/h</td><td>25:00</td><td>50:00</td><td>1:45:29</td><td>3:30:59</td></tr>
        <tr><td>5'30"</td><td>10.9 km/h</td><td>27:30</td><td>55:00</td><td>1:56:02</td><td>3:52:04</td></tr>
        <tr><td>6'00"</td><td>10.0 km/h</td><td>30:00</td><td>1:00:00</td><td>2:06:35</td><td>4:13:10</td></tr>
        <tr><td>6'30"</td><td>9.2 km/h</td><td>32:30</td><td>1:05:00</td><td>2:17:08</td><td>4:34:16</td></tr>
        <tr><td>7'00"</td><td>8.6 km/h</td><td>35:00</td><td>1:10:00</td><td>2:27:41</td><td>4:55:22</td></tr>
      </table>
    </div>
    <p>
      A 30-second difference in pace grows to about 21 minutes over a full marathon. The longer the race, the more a slightly
      fast start comes back to haunt you later, so once you have chosen a target pace, avoid running faster than it early on.
    </p>

    <h2>Setting pace by training purpose</h2>
    <p>
      Running every session at the same pace tends to build fatigue without much improvement. Splitting your training by
      intensity makes the same amount of time far more effective. The guidelines below are rough and based on your recent 5K race pace.
    </p>
    <h3>Easy runs (the foundation of most of your running)</h3>
    <p>
      A comfortable effort at which you can speak in full sentences — roughly 1 to 1.5 minutes per km slower than your 5K pace.
      If you run 5K in 30 minutes (6:00 pace), your easy pace is about 7:00–7:30. It should feel almost too slow; that is the
      right intensity, and it builds aerobic endurance and recovery capacity. Many training approaches recommend spending
      about 80% of your running time at this easy intensity.
    </p>
    <h3>Tempo runs (comfortably hard)</h3>
    <p>
      An effort at which you can give short answers but not hold a conversation, sustained for 20–30 minutes. Aim for about
      20–30 seconds per km slower than your 5K pace. Tempo runs build your ability to hold a steady speed for a long time,
      which helps your 10K and half marathon times.
    </p>
    <h3>Intervals (short and fast repeats)</h3>
    <p>
      Run 400 m to 1 km at or slightly faster than your 5K pace, then walk or jog slowly for the same amount of time to recover,
      and repeat 4–8 times. Because intervals are intense, once a week is usually enough, and beginners are better off building
      a base with easy runs first.
    </p>

    <h2>Why GPS pace jumps around</h2>
    <p>
      You have probably seen your current pace suddenly jump to 3 or 10 minutes per km mid-run. Smartphone GPS becomes less
      accurate between tall buildings, under overpasses and beneath dense trees, so the instantaneous pace over short stretches
      can be wrong. <strong>Per-kilometer splits or your overall average pace</strong> are more reliable. Running the same course
      repeatedly also makes comparisons easier, because course-specific errors show up similarly each time.
    </p>

    <div class="guide-note">
      <p>
        <strong>In Pace League,</strong> your average pace is calculated automatically from the GPS distance and time when you finish a run,
        and a score that reflects distance and pace is added to your season ranking and tier. Improving your pace raises your score
        for the same distance, so build your base with easy runs and mix in the occasional tempo run or interval session.
      </p>
    </div>
"""

BODY['first-5k'] = """
    <p class="guide-lead">
      Even if you have never run 5 km without stopping, eight weeks is enough to get there. The key is not trying to run
      continuously from day one. This plan alternates walking and running and gradually increases the running time, so that
      by week 8 you can run for 30 minutes straight.
    </p>

    <h2>Three things to know before you start</h2>
    <ol>
      <li><strong>Ignore speed.</strong> Run slowly enough that you could still say a short sentence, not so hard that you can't talk. At first it may feel barely faster than brisk walking, and that is fine.</li>
      <li><strong>Train three times a week with a rest day in between.</strong> Mon/Wed/Fri or Tue/Thu/Sat works well. Your muscles and joints adapt and get stronger on rest days.</li>
      <li><strong>Walk for 5 minutes before and after every session.</strong> The times in the table are for the main workout only; add 5 minutes of brisk walking at each end.</li>
    </ol>

    <h2>Week-by-week plan</h2>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Week</th><th>Main workout (3× per week)</th><th>Total running time</th></tr>
        <tr><td>1</td><td>Run 1 min + walk 2 min × 8</td><td>8 min</td></tr>
        <tr><td>2</td><td>Run 2 min + walk 2 min × 6</td><td>12 min</td></tr>
        <tr><td>3</td><td>Run 3 min + walk 2 min × 5</td><td>15 min</td></tr>
        <tr><td>4</td><td>Run 5 min + walk 2 min × 4</td><td>20 min</td></tr>
        <tr><td>5</td><td>Run 8 min + walk 2 min × 3</td><td>24 min</td></tr>
        <tr><td>6</td><td>Run 12 min + walk 2 min × 2</td><td>24 min</td></tr>
        <tr><td>7</td><td>Run 20 min continuously (22 min in the 3rd session)</td><td>20–22 min</td></tr>
        <tr><td>8</td><td>Run 25 min continuously → try 5 km or 30 min in the last session</td><td>25–30 min</td></tr>
      </table>
    </div>
    <p>
      If a week feels too hard, <strong>simply repeat it</strong>. It is completely fine if eight weeks turns into ten or twelve.
      On the other hand, skipping weeks because something feels easy is not recommended. Your heart and lungs improve quickly,
      but joints and tendons adapt more slowly, so pushing harder just because breathing feels easier often leads to knee or shin pain.
    </p>

    <h2>Key points for each stage</h2>
    <h3>Weeks 1–2: build the habit</h3>
    <p>
      The goal here is not fitness but <strong>the habit of getting out the door on set days</strong>. If you get out of breath during
      the 1–2 minute runs, slow down even more. The walking breaks are part of the workout, not a rest, so keep walking rather than stopping.
    </p>
    <h3>Weeks 3–5: extend the running</h3>
    <p>
      As the running segments grow to 3, 5 and 8 minutes, you will feel real effort for the first time. Breathe through both nose
      and mouth, relax your shoulders and hands, and shorten your stride so your feet land closer beneath your body — it makes a big difference.
    </p>
    <h3>Weeks 6–8: continuous running</h3>
    <p>
      In week 7 you run 20 minutes without stopping for the first time. It is the biggest mental hurdle, but you already ran
      12 minutes twice (24 minutes in total) in weeks 5–6, so your fitness is there. The secret is to start the first 10 minutes
      even slower than usual. In the final session of week 8, aim for 5 km or 30 minutes, whichever comes first.
      A first 5K of 35–40 minutes is an excellent result for a beginner.
    </p>

    <h2>Tips for sticking with it</h2>
    <ul>
      <li><strong>Check your shoes first.</strong> Cushioned running shoes put less strain on a beginner's joints than thin-soled everyday sneakers. Try them on in store and leave about a thumb's width of space in front of your toes.</li>
      <li><strong>Keep a record.</strong> Writing down the date, running time and how you felt lets you see how much easier things are after 2–3 weeks — the best motivation there is.</li>
      <li><strong>Don't run through pain.</strong> Muscle soreness is normal, but if one spot hurts sharply or you start limping, stop and rest for a few days. If the pain persists, see a doctor.</li>
      <li><strong>Have a bad-weather backup.</strong> On days with rain or poor air quality, a treadmill or brisk walk keeps the habit going.</li>
    </ul>

    <div class="guide-note">
      <p>
        <strong>In Pace League,</strong> recording your run in the app automatically calculates distance and average pace and adds it to your history,
        and your season score and tier rise as you run. Watch your progress over the eight weeks, and on the day you finish your
        first 5K, share it in the community's verification board with your run attached.
      </p>
    </div>
"""

BODY['injury-prevention'] = """
    <p class="guide-lead">
      Running needs no special equipment, but because it repeats the same motion thousands of times, overuse injuries are common.
      Fortunately, many running injuries can be reduced just by paying attention to how quickly you add mileage and by
      warming up and cooling down. This guide covers the causes of common injuries and habits you can start today.
    </p>

    <div class="guide-note">
      <p>This article is general exercise information and does not replace medical diagnosis or treatment. If pain persists or there is swelling, consult a doctor.</p>
    </div>

    <h2>Common running injuries</h2>
    <ul>
      <li><strong>Pain at the front of the knee</strong>: often an ache around the kneecap or pain when walking downstairs. Commonly called "runner's knee", it is often linked to weak thigh and hip muscles or a sudden jump in mileage.</li>
      <li><strong>Shin pain</strong>: a throbbing pain along the inner edge of the shinbone, common when you have just started running or suddenly increase distance on hard surfaces.</li>
      <li><strong>Pain under the foot</strong>: if the underside of your heel stings with your first steps in the morning, it may be a sign of strain on the plantar fascia.</li>
      <li><strong>Achilles tendon pain</strong>: stiffness and pain in the tendon above the heel, which tends to appear after a sudden increase in hill or speed work.</li>
    </ul>
    <p>
      What these injuries have in common is that <strong>training load increased faster than the body could adapt</strong>.
      Aerobic fitness improves within weeks, but bones, tendons and ligaments take months to adapt. Increasing distance and
      speed at the same time just because breathing feels easier opens up exactly that gap.
    </p>

    <h2>Add distance slowly, one thing at a time</h2>
    <p>
      A widely used rule of thumb among runners is to <strong>increase total weekly distance by no more than about 10% over the previous week</strong>.
      If you ran 20 km this week, around 22 km next week is reasonable. It is not a precise scientific threshold, but it is a
      useful guard against sudden jumps. A few related principles:
    </p>
    <ul>
      <li><strong>Don't increase distance and intensity together.</strong> In a week when you add distance, don't add intervals or hill work.</li>
      <li><strong>Make every 3rd or 4th week an easier week.</strong> Cut weekly distance by 20–30% to give your body time to recover.</li>
      <li><strong>Limit back-to-back running days.</strong> Beginners do well to put a rest day or an easy walk between runs.</li>
    </ul>

    <h2>A 5–10 minute warm-up routine</h2>
    <p>
      Speeding up on cold muscles raises injury risk. Before running, a dynamic warm-up that moves your joints through their
      range of motion is more suitable than static stretching (holding one position for a long time).
    </p>
    <ol>
      <li>3–5 minutes of brisk walking or very light jogging</li>
      <li>Leg swings: holding a wall, 10 swings front-to-back and 10 side-to-side per leg</li>
      <li>Walking lunges: 10 big steps</li>
      <li>High knees and butt kicks: 20 seconds each in place</li>
      <li>Run the first kilometer of your main run slower than your target</li>
    </ol>

    <h2>Cool-down and recovery</h2>
    <p>
      Rather than stopping as soon as you finish, walk or jog slowly for 3–5 minutes to bring your heart rate down, then hold
      static stretches for your calves, front and back of the thighs and hips for 20–30 seconds each. Good sleep, staying hydrated,
      and getting protein and carbohydrates in your post-run meal also help you recover for the next run.
    </p>

    <h2>10 minutes of strength work, twice a week</h2>
    <p>
      Running alone does not make the hip, thigh and calf muscles that absorb landing forces strong enough.
      Investing just 10 minutes on non-running days helps reduce stress on your knees and ankles.
    </p>
    <ul>
      <li>Squats: 3 sets of 15</li>
      <li>Glute bridges (lying down, lift the hips): 3 sets of 15</li>
      <li>Calf raises: 3 sets of 20</li>
      <li>Plank: 3 × 30 seconds</li>
    </ul>

    <h2>When to replace running shoes</h2>
    <p>
      Shoe cushioning wears down gradually and invisibly. Around <strong>500–800 km</strong> is commonly cited as replacement time,
      and shoes can wear out sooner depending on body weight and running form. If the sole is heavily worn on one side, or new
      leg pain disappears when you switch to a new pair, you have gone past replacement time. Logging your distance makes it easier to judge.
    </p>

    <h2>When you should rest</h2>
    <ul>
      <li>A specific spot hurts when pressed and the pain gets worse the more you run</li>
      <li>There is swelling or you start limping</li>
      <li>The pain doesn't ease after several days of rest</li>
    </ul>
    <p>
      A few days off barely affects your fitness, but running through pain until the injury gets worse can cost you weeks or months.
    </p>

    <div class="guide-note">
      <p>
        <strong>In Pace League,</strong> your runs are logged by date, so you can easily compare this week's total distance with last week's.
        When increasing distance, check your weekly increase on the records screen and plan so you don't overdo it.
      </p>
    </div>
"""

BODY['landit-strategy'] = """
    <p class="guide-lead">
      Landeat is Pace League's running game in which your route claims land on a real map. Even when you run the same 5 km,
      the area you capture can differ several times over depending on the shape of your route. This guide covers when land counts,
      how to design routes that maximize area, and how to take land from other runners.
    </p>

    <h2>When land counts</h2>
    <p>Start a run with Landeat mode on; if all of the conditions below are met, land is created the moment your run ends.</p>
    <ul>
      <li><strong>You must return to where you started.</strong> The end point must be within 50 m of the start point for the route to count as a closed loop.</li>
      <li><strong>The perimeter must be at least 300 m.</strong> Loops that are too short don't count.</li>
      <li><strong>The area must be between 10,000 m² (about 100 m × 100 m) and 5 km².</strong> Loops that are too small or unrealistically large are excluded.</li>
    </ul>
    <p>
      Once these checks pass, the area enclosed by your loop is converted into <strong>hexagonal tiles</strong> on the map. You get not
      only the tiles inside the route but also every tile the route passes through. Zoom in far enough on the map and you can see
      this hexagon grid yourself, so you can check tile by tile where each runner's land begins and ends.
    </p>

    <h2>Maximize area for the same distance: shape is everything</h2>
    <p>
      For the same perimeter, the closer a shape is to a circle, the more area it encloses. For example, for a 1.2 km loop:
    </p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Loop shape (1.2 km perimeter)</th><th>Enclosed area</th></tr>
        <tr><td>Rectangle 100 m wide × 500 m long</td><td>about 50,000 m²</td></tr>
        <tr><td>Square 300 m × 300 m (one city block)</td><td>about 90,000 m²</td></tr>
        <tr><td>Circle with a radius of about 191 m</td><td>about 115,000 m²</td></tr>
      </table>
    </div>
    <p>
      For the same 1.2 km, a long narrow loop claims less than half the land of a circular one. On real streets you can't run a
      perfect circle, so <strong>running around a block whose width and length are similar</strong> is the most practical choice.
      Out-and-back routes enclose almost no area and won't create land, so watch out for them.
    </p>
    <p>
      Note that one lap of a 400 m athletics track encloses only about 10,000 m², right on the edge of the minimum. A small GPS
      error can push it below the threshold, so a park perimeter or a residential block is a safer choice than a track.
    </p>

    <h2>One big loop vs several small ones</h2>
    <p>
      Area grows with the square of the perimeter: double the perimeter and the area becomes roughly four times larger. That is why
      one 3 km loop captures far more land than three 1 km loops. If you have the legs for it, a big lap around your neighborhood
      is best for the area ranking. A single loop larger than 5 km² doesn't count, but that is roughly a square 2.2 km on each side,
      so normal runs almost never hit it.
    </p>

    <h2>Taking other runners' land</h2>
    <p>
      When your new loop overlaps someone else's land, <strong>the overlapping hexagonal tiles</strong> become yours. The rule is simple:
      <strong>the last runner to cover a tile owns it</strong>. It doesn't matter who claimed it first or how long they have held it.
    </p>
    <ul>
      <li>You don't need to cover the other runner's whole territory. Even a partial overlap gives you every overlapping tile, and their territory shrinks to the tiles that remain.</li>
      <li>If you cover every tile of someone's territory, it disappears from the map.</li>
      <li>Tiles that are already yours stay as they are when you run over them again; only newly covered empty tiles and captured tiles are grouped into your new land.</li>
    </ul>
    <p>
      The flip side is that your land can be taken at any time. If other runners are active around your usual routes, re-run the
      same loop regularly to win back lost tiles. The Landeat ranking on the map screen shows each runner's total area, so it can
      also help to see where the top runners' land is and plan your routes accordingly.
    </p>

    <h2>A checklist so your run counts</h2>
    <ol>
      <li>Before starting, make sure Landeat mode is on.</li>
      <li>Remember your starting point and always press finish within 50 m of it.</li>
      <li>Avoid loops where most of the route runs through weak-GPS areas such as between tall buildings or through tunnels.</li>
      <li>Choose a route that circles in one direction instead of an out-and-back.</li>
    </ol>

    <div class="guide-note">
      <p>
        <strong>Safety first.</strong> Don't cut across roads, enter restricted private property or construction sites, or run in deserted
        places late at night just to claim more land. Every piece of land can be claimed using public roads and sidewalks alone.
      </p>
    </div>
"""


# ---------------------------------------------------------------- 소개 페이지 (/about)
ABOUT = {'title': 'About', 'desc': 'Pace League is a running service that brings run tracking with pace and calorie calculation, rankings and tiers, the Landeat land-grab running game, running crews and a community together in one place.', 'h1': 'Pace League: a running service that gives you a reason to run every day', 'lead': 'Pace League is more than an app that logs how far you ran. It turns every run into a score and a tier so you can compete, lets you claim land on a real map with your GPS route, helps you form crews to run with, and lets you share runs and stories with other runners — all built to turn "running alone" into "running together, and running consistently".', 'sections': [('clock', 'Run tracking with automatic pace and calorie calculation', 'When you start a run in the app, your GPS coordinates are sent to the server in real time and stored as a single running session. When you finish, your average pace (min/km) and calories burned are calculated automatically from distance and time and added to your running history. No calculator and no manual entry — just run, and your records build up.'), ('trophy', 'Rankings and tiers', 'Your runs are converted into a score that reflects distance and pace, and each season that score places you in a tier from Bronze up to the top tier. Along with the full season ranking, a TOP 10 ranking right on the home screen shows at a glance where you stand among other runners.'), ('pin', 'Landeat — a running game where you claim land', "Landeat is Pace League's own map-based running game. Run a closed loop of a minimum length that returns to your starting point, and the area enclosed by your route is converted into hexagonal tiles (land) on a real map that become yours. If your route overlaps land another runner already holds, you can take that part from them, so even running in the same neighborhood calls for a different strategy each time. The map screen shows your land, other runners' land, and the Landeat ranking by total area."), ('users', 'Crews — running teams', 'If you would rather run as a team than alone, you can create a crew or join an existing one. Each person belongs to only one crew, and crews are run by a leader who invites other runners or approves join requests. Once you join, your crew badge appears next to your posts and in the rankings, so other runners can see which crew you belong to.'), ('chat', 'Community', 'The community is divided into Free talk, Q&amp;A, Run verification and Crew promotion boards where you can share runs, Landeat strategies and crew news. You can attach photos and videos straight from the editor and attach your own run to a post as proof. Anyone can browse the boards and read posts without logging in; only writing, commenting and voting require an account.')], 'cta': 'Record your first run today.', 'join': 'Sign up', 'login': 'Log in', 'links': {'guide': 'Running Guide', 'privacy': 'Privacy Policy', 'terms': 'Terms of Service', 'location': 'Location-Based Service Terms'}}
