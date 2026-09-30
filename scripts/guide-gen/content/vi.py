# -*- coding: utf-8 -*-
"""Hướng dẫn tiếng Việt (dịch từ ko.py)."""

UI = dict(crumb="Hướng dẫn chạy bộ", author="Đội ngũ Pace League", date="30 tháng 9, 2026", related="Hướng dẫn khác")

HUB = dict(title="Hướng dẫn chạy bộ",
           desc="Tính pace, kế hoạch 5 km trong 8 tuần cho người mới, phòng tránh chấn thương và chiến thuật Landeat — bộ hướng dẫn chạy bộ của Pace League.",
           lead="Dù bạn mới bắt đầu chạy hay đang muốn phá kỷ lục cá nhân, đây là những điều cần biết để chạy đều đặn và vui vẻ. Chúng tôi cũng giới thiệu cách tận dụng tốt hơn bảng xếp hạng và Landeat của Pace League.")

ARTICLES = {
  'pace': dict(tag='Cơ bản', title='Hiểu về pace chạy bộ: từ cách tính đến pace theo cường độ tập',
       desc='Ý nghĩa và cách tính pace (phút/km), quy đổi sang tốc độ, bảng thời gian hoàn thành 5 km, 10 km, bán marathon và marathon, cùng cách đặt pace cho chạy nhẹ, chạy tempo và chạy biến tốc.',
       summary='Mối liên hệ giữa pace và tốc độ, bảng quy đổi thời gian hoàn thành và pace phù hợp cho chạy nhẹ, tempo và biến tốc.'),
  'first-5k': dict(tag='Nhập môn', title='Kế hoạch 8 tuần hoàn thành 5 km cho người mới chạy',
       desc='Bảng tập theo từng tuần kết hợp đi bộ và chạy để ngay cả người ít vận động cũng có thể chạy 5 km không nghỉ sau 8 tuần, kèm các mẹo thực tế.',
       summary='Bắt đầu bằng xen kẽ đi bộ và chạy, 8 tuần sau chạy 5 km không nghỉ.'),
  'injury-prevention': dict(tag='Sức khỏe', title='Phòng tránh chấn thương khi chạy: khởi động, thả lỏng và cách tăng quãng đường',
       desc='Nguyên nhân và cách phòng tránh các chấn thương thường gặp ở đầu gối, ống chân và bàn chân, bài khởi động và thả lỏng, cách tăng quãng đường mỗi tuần an toàn và thời điểm thay giày.',
       summary='Chấn thương thường gặp khi chạy, bài khởi động và thả lỏng, cách tăng quãng đường mỗi tuần an toàn.'),
  'landit-strategy': dict(tag='Landeat', title='Chiến thuật Landeat: lộ trình chiếm được nhiều đất hơn với cùng quãng đường',
       desc='Luật chiếm đất của Landeat — trò chơi chạy bộ của Pace League, cách thiết kế lộ trình bao được nhiều diện tích hơn với cùng quãng đường và mẹo giành đất từ người chạy khác.',
       summary='Khi nào đất được công nhận, hình dạng vòng chạy nào cho diện tích lớn nhất và cách giành đất từ người khác.'),
}

BODY = {}

BODY['pace'] = """
    <p class="guide-lead">
      Pace là con số đầu tiên bạn thấy khi mở ứng dụng chạy bộ. Nhưng nhiều người không rõ “pace 5:30” là nhanh hay chậm,
      hay hôm nay nên chạy ở pace nào. Bài viết này giải thích ý nghĩa của pace, cách tính, cách đổi thời gian mục tiêu sang pace
      và cách điều chỉnh pace theo mục đích của từng buổi tập.
    </p>

    <h2>Pace là gì: thời gian chạy 1 km</h2>
    <p>
      Trong chạy bộ, pace là <strong>thời gian bạn cần để chạy hết 1 km</strong>, thường được viết bằng phút và giây như <code>5'30"/km</code>.
      Người chạy dùng pace thay vì tốc độ (km/h) vì nó trực quan hơn: nếu muốn chạy 10 km dưới 55 phút, bạn biết ngay mỗi km phải chạy
      trong 5 phút 30 giây hoặc nhanh hơn.
    </p>
    <p>
      Với pace, <strong>con số càng nhỏ càng nhanh</strong>. Pace 5 phút nhanh hơn pace 6 phút một phút cho mỗi km. Người mới bắt đầu thường
      chạy ở pace 7–8 phút, còn người chạy phong trào tập đều đặn thường chạy thoải mái quanh 5 phút. Tuy nhiên, pace thay đổi nhiều theo cân nặng,
      tuổi, độ dốc của cung đường và thời tiết, nên nó hữu ích hơn để theo dõi <strong>sự tiến bộ của chính bạn</strong> thay vì so sánh với người khác.
    </p>

    <h2>Cách tính pace và quy đổi sang tốc độ</h2>
    <p>Pace = <strong>tổng thời gian chạy ÷ quãng đường (km)</strong>.</p>
    <ul>
      <li>Chạy 5 km trong 30 phút: 30 ÷ 5 = <strong>6:00 mỗi km</strong></li>
      <li>Chạy 10 km trong 58 phút: 58 ÷ 10 = 5,8 phút = <strong>5:48 mỗi km</strong> (0,8 × 60 = 48 giây)</li>
      <li>Chạy 3,2 km trong 20 phút: 20 ÷ 3,2 = 6,25 phút = <strong>6:15 mỗi km</strong></li>
    </ul>
    <p>
      Tốc độ và pace liên hệ với nhau theo công thức <strong>pace (phút) = 60 ÷ tốc độ (km/h)</strong>. Máy chạy đặt 10 km/h tương đương
      60 ÷ 10 = pace 6 phút; 12 km/h là pace 5 phút. Rất tiện khi so sánh buổi tập trên máy với chạy ngoài trời.
    </p>

    <h2>Bảng thời gian hoàn thành theo pace</h2>
    <p>Nếu có thời gian mục tiêu cho giải chạy, bạn có thể tra bảng dưới đây để tìm pace trung bình cần thiết. (Bán marathon 21,0975 km, marathon 42,195 km)</p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Pace (/km)</th><th>Tốc độ</th><th>5 km</th><th>10 km</th><th>Bán marathon</th><th>Marathon</th></tr>
        <tr><td>4'30"</td><td>13,3 km/h</td><td>22:30</td><td>45:00</td><td>1:34:56</td><td>3:09:53</td></tr>
        <tr><td>5'00"</td><td>12,0 km/h</td><td>25:00</td><td>50:00</td><td>1:45:29</td><td>3:30:59</td></tr>
        <tr><td>5'30"</td><td>10,9 km/h</td><td>27:30</td><td>55:00</td><td>1:56:02</td><td>3:52:04</td></tr>
        <tr><td>6'00"</td><td>10,0 km/h</td><td>30:00</td><td>1:00:00</td><td>2:06:35</td><td>4:13:10</td></tr>
        <tr><td>6'30"</td><td>9,2 km/h</td><td>32:30</td><td>1:05:00</td><td>2:17:08</td><td>4:34:16</td></tr>
        <tr><td>7'00"</td><td>8,6 km/h</td><td>35:00</td><td>1:10:00</td><td>2:27:41</td><td>4:55:22</td></tr>
      </table>
    </div>
    <p>
      Nhìn bảng có thể thấy chênh lệch 30 giây pace sẽ thành khoảng 21 phút ở cự ly marathon. Cự ly càng dài, việc chạy hơi nhanh ở đầu
      càng khiến bạn trả giá ở nửa sau, vì vậy khi đã chọn pace mục tiêu, đừng chạy nhanh hơn nó ở giai đoạn đầu.
    </p>

    <h2>Đặt pace theo mục đích tập luyện</h2>
    <p>
      Buổi nào cũng chạy cùng một pace thường chỉ tích lũy mệt mỏi mà ít tiến bộ. Chia bài tập theo cường độ sẽ giúp cùng một lượng thời gian
      đem lại hiệu quả khác hẳn. Các mốc dưới đây là tham khảo tương đối, dựa trên pace 5 km gần đây của bạn.
    </p>
    <h3>Chạy nhẹ (nền tảng của phần lớn các buổi chạy)</h3>
    <p>
      Cường độ thoải mái, có thể nói chuyện thành câu với người bên cạnh — chậm hơn pace 5 km khoảng 1 phút đến 1 phút 30 giây mỗi km.
      Nếu bạn chạy 5 km trong 30 phút (pace 6:00), pace chạy nhẹ của bạn khoảng 7:00–7:30. Cảm giác “chậm thế này có ổn không” mới là đúng
      cường độ, giúp xây dựng sức bền tim phổi và khả năng hồi phục. Nhiều phương pháp tập khuyên dành khoảng 80% thời gian chạy cho cường độ nhẹ này.
    </p>
    <h3>Chạy tempo (khó mà vẫn kiểm soát được)</h3>
    <p>
      Cường độ có thể trả lời ngắn nhưng khó duy trì cuộc trò chuyện, giữ trong 20–30 phút. Mục tiêu là chậm hơn pace 5 km khoảng 20–30 giây mỗi km.
      Chạy tempo giúp tăng khả năng giữ tốc độ đều trong thời gian dài, có ích cho thành tích 10 km và bán marathon.
    </p>
    <h3>Chạy biến tốc (lặp lại ngắn và nhanh)</h3>
    <p>
      Chạy 400 m đến 1 km ở pace 5 km hoặc nhanh hơn một chút, rồi đi bộ hoặc chạy chậm để hồi phục trong khoảng thời gian tương đương, lặp lại 4–8 lần.
      Vì cường độ cao nên mỗi tuần khoảng một lần là đủ, và người mới nên xây nền thể lực bằng chạy nhẹ trước.
    </p>

    <h2>Vì sao pace GPS nhảy lung tung</h2>
    <p>
      Có lẽ bạn từng thấy pace hiện tại đột nhiên nhảy lên 3 phút hay 10 phút giữa chừng. GPS điện thoại kém chính xác hơn khi ở giữa các tòa nhà cao,
      dưới cầu vượt hoặc dưới tán cây rậm, nên pace tức thời ở đoạn ngắn có thể sai. <strong>Pace từng km hoặc pace trung bình cả buổi</strong>
      đáng tin cậy hơn. Chạy lặp lại cùng một cung đường cũng giúp so sánh thành tích dễ hơn, vì sai số riêng của cung đường xuất hiện tương tự mỗi lần.
    </p>

    <div class="guide-note">
      <p>
        <strong>Trên Pace League,</strong> sau khi kết thúc buổi chạy, pace trung bình được tự động tính từ quãng đường và thời gian đo bằng GPS,
        và điểm phản ánh quãng đường cùng pace sẽ được cộng vào bảng xếp hạng mùa và hạng của bạn. Cùng quãng đường nhưng pace tốt hơn thì điểm cao hơn,
        vì vậy hãy xây nền bằng chạy nhẹ và thỉnh thoảng thêm chạy tempo hoặc biến tốc để nâng pace.
      </p>
    </div>
"""

BODY['first-5k'] = """
    <p class="guide-lead">
      Ngay cả khi bạn chưa từng chạy 5 km không nghỉ, 8 tuần là đủ để làm được. Điều quan trọng là đừng cố chạy liên tục ngay từ ngày đầu.
      Kế hoạch này xen kẽ đi bộ và chạy, tăng dần thời gian chạy để đến tuần thứ 8 bạn có thể chạy liên tục 30 phút.
    </p>

    <h2>Ba điều cần biết trước khi bắt đầu</h2>
    <ol>
      <li><strong>Đừng bận tâm đến tốc độ.</strong> Khi chạy, hãy chạy đủ chậm để vẫn nói được một câu ngắn, không đến mức thở dốc không nói nổi. Lúc đầu có thể chẳng khác đi bộ nhanh là mấy, và điều đó hoàn toàn ổn.</li>
      <li><strong>Tập 3 buổi mỗi tuần, nghỉ xen kẽ một ngày.</strong> Thứ Hai/Tư/Sáu hoặc Thứ Ba/Năm/Bảy đều phù hợp. Cơ và khớp thích nghi và mạnh lên vào những ngày nghỉ.</li>
      <li><strong>Đi bộ 5 phút trước và sau mỗi buổi tập.</strong> Thời gian trong bảng chỉ là phần tập chính; hãy thêm 5 phút đi bộ nhanh ở đầu và cuối.</li>
    </ol>

    <h2>Bảng tập theo tuần</h2>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Tuần</th><th>Bài tập chính (3 buổi/tuần)</th><th>Tổng thời gian chạy</th></tr>
        <tr><td>1</td><td>Chạy 1 phút + đi bộ 2 phút × 8 lần</td><td>8 phút</td></tr>
        <tr><td>2</td><td>Chạy 2 phút + đi bộ 2 phút × 6 lần</td><td>12 phút</td></tr>
        <tr><td>3</td><td>Chạy 3 phút + đi bộ 2 phút × 5 lần</td><td>15 phút</td></tr>
        <tr><td>4</td><td>Chạy 5 phút + đi bộ 2 phút × 4 lần</td><td>20 phút</td></tr>
        <tr><td>5</td><td>Chạy 8 phút + đi bộ 2 phút × 3 lần</td><td>24 phút</td></tr>
        <tr><td>6</td><td>Chạy 12 phút + đi bộ 2 phút × 2 lần</td><td>24 phút</td></tr>
        <tr><td>7</td><td>Chạy liên tục 20 phút (buổi thứ 3: 22 phút)</td><td>20–22 phút</td></tr>
        <tr><td>8</td><td>Chạy liên tục 25 phút → buổi cuối thử 5 km hoặc 30 phút</td><td>25–30 phút</td></tr>
      </table>
    </div>
    <p>
      Nếu tuần nào khó theo kịp, <strong>hãy lặp lại tuần đó thêm một lần</strong>. 8 tuần thành 10 hay 12 tuần cũng hoàn toàn không sao.
      Ngược lại, không nên bỏ qua tuần nào chỉ vì thấy dễ. Tim phổi tiến bộ nhanh, nhưng khớp và gân thích nghi chậm hơn, nên cố quá sức
      chỉ vì thấy thở dễ hơn thường dẫn đến đau đầu gối hoặc ống chân.
    </p>

    <h2>Điểm chính của từng giai đoạn</h2>
    <h3>Tuần 1–2: tạo thói quen</h3>
    <p>
      Mục tiêu giai đoạn này không phải thể lực mà là <strong>thói quen ra ngoài vào những ngày đã định</strong>. Nếu thấy hụt hơi trong 1–2 phút chạy,
      hãy giảm tốc độ thêm nữa. Phần đi bộ không phải là nghỉ mà là một phần của bài tập, vì vậy hãy tiếp tục đi, đừng dừng lại.
    </p>
    <h3>Tuần 3–5: kéo dài thời gian chạy</h3>
    <p>
      Khi các đoạn chạy dài dần lên 3, 5 rồi 8 phút, đây là lúc bạn lần đầu thấy “mệt”. Hãy thở bằng cả mũi và miệng, thả lỏng vai và tay,
      thu ngắn sải chân để bàn chân tiếp đất gần ngay dưới thân người — bạn sẽ thấy nhẹ nhàng hơn nhiều.
    </p>
    <h3>Tuần 6–8: chạy liên tục</h3>
    <p>
      Tuần 7 là lần đầu bạn chạy 20 phút không nghỉ. Đây là rào cản tâm lý lớn nhất, nhưng ở tuần 5–6 bạn đã chạy hai lần 12 phút,
      tổng cộng 24 phút, nên thể lực đã đủ. Bí quyết là bắt đầu 10 phút đầu còn chậm hơn bình thường. Ở buổi cuối của tuần 8, hãy chạy với mục tiêu
      5 km hoặc 30 phút, tùy điều nào đến trước. Với người mới, lần chạy 5 km đầu tiên mất 35–40 phút đã là thành tích tuyệt vời.
    </p>

    <h2>Mẹo để duy trì lâu dài</h2>
    <ul>
      <li><strong>Hãy kiểm tra giày trước.</strong> Giày chạy có đệm giúp giảm áp lực lên khớp của người mới hơn giày thể thao đế mỏng dùng hằng ngày. Hãy thử giày tại cửa hàng và chọn cỡ còn khoảng một bề ngang ngón cái phía trước mũi chân.</li>
      <li><strong>Ghi lại quá trình.</strong> Ghi ngày, thời gian chạy và tình trạng cơ thể, sau 2–3 tuần bạn sẽ thấy mình nhẹ nhàng hơn rõ rệt — đó là động lực lớn nhất.</li>
      <li><strong>Đừng cố chịu đau.</strong> Mỏi cơ là bình thường, nhưng nếu một chỗ đau nhói hoặc bạn bắt đầu đi khập khiễng, hãy dừng tập và nghỉ vài ngày. Nếu cơn đau kéo dài, nên đi khám bác sĩ.</li>
      <li><strong>Chuẩn bị phương án cho ngày thời tiết xấu.</strong> Những hôm mưa hoặc không khí ô nhiễm, chạy máy trong nhà hay đi bộ nhanh vẫn giúp duy trì thói quen.</li>
    </ul>

    <div class="guide-note">
      <p>
        <strong>Trên Pace League,</strong> khi ghi lại buổi chạy bằng ứng dụng, quãng đường và pace trung bình được tính tự động và lưu vào lịch sử của bạn,
        chạy càng nhiều thì điểm mùa và hạng càng tăng. Hãy tự mình theo dõi sự thay đổi trong 8 tuần, và vào ngày hoàn thành 5 km,
        hãy chia sẻ trên bảng xác nhận của cộng đồng kèm buổi chạy của bạn.
      </p>
    </div>
"""

BODY['injury-prevention'] = """
    <p class="guide-lead">
      Chạy bộ không cần dụng cụ đặc biệt, nhưng vì lặp lại cùng một động tác hàng nghìn lần nên chấn thương do dùng quá mức rất phổ biến.
      May mắn là nhiều chấn thương có thể giảm bớt chỉ bằng cách chú ý tốc độ tăng quãng đường cùng với khởi động và thả lỏng.
      Bài viết này tổng hợp nguyên nhân của các chấn thương thường gặp và những thói quen phòng tránh bạn có thể bắt đầu ngay hôm nay.
    </p>

    <div class="guide-note">
      <p>Bài viết này là thông tin chung về luyện tập, không thay thế chẩn đoán hay điều trị y khoa. Nếu cơn đau kéo dài hoặc có sưng, hãy tham khảo ý kiến bác sĩ.</p>
    </div>

    <h2>Chấn thương thường gặp ở người chạy bộ</h2>
    <ul>
      <li><strong>Đau phía trước đầu gối</strong>: thường là cảm giác ê ẩm quanh xương bánh chè hoặc đau khi xuống cầu thang. Hay được gọi là “đầu gối người chạy”, thường liên quan đến cơ đùi và hông yếu hoặc tăng quãng đường đột ngột.</li>
      <li><strong>Đau ống chân</strong>: cơn đau âm ỉ dọc mép trong xương ống chân, hay gặp khi mới bắt đầu chạy hoặc đột ngột tăng quãng đường trên mặt đường cứng.</li>
      <li><strong>Đau lòng bàn chân</strong>: nếu phần lòng bàn chân phía gót nhói lên ở những bước đầu tiên buổi sáng, đó có thể là dấu hiệu cân gan chân bị quá tải.</li>
      <li><strong>Đau gân Achilles</strong>: gân phía trên gót chân cứng và đau, dễ xuất hiện khi đột ngột tăng bài tập leo dốc hoặc tốc độ.</li>
    </ul>
    <p>
      Điểm chung của các chấn thương này là <strong>khối lượng tập tăng nhanh hơn tốc độ cơ thể thích nghi</strong>.
      Sức bền tim phổi cải thiện trong vài tuần, nhưng xương, gân và dây chằng cần vài tháng để thích nghi. Thấy thở dễ hơn liền tăng cả quãng đường
      lẫn tốc độ cùng lúc chính là tạo ra khoảng chênh lệch đó.
    </p>

    <h2>Tăng quãng đường từ từ, mỗi lần một thứ</h2>
    <p>
      Một quy tắc kinh nghiệm phổ biến trong giới chạy bộ là <strong>chỉ tăng tổng quãng đường mỗi tuần khoảng 10% so với tuần trước</strong>.
      Tuần này chạy 20 km thì tuần sau khoảng 22 km là hợp lý. Đây không phải con số khoa học chính xác, nhưng đủ để làm mốc tránh tăng đột ngột.
      Một số nguyên tắc cần nhớ thêm:
    </p>
    <ul>
      <li><strong>Không tăng quãng đường và cường độ cùng lúc.</strong> Tuần tăng quãng đường thì không thêm bài biến tốc hay leo dốc.</li>
      <li><strong>Cứ 3–4 tuần có một tuần tập nhẹ.</strong> Giảm quãng đường tuần 20–30% để cơ thể có thời gian hồi phục.</li>
      <li><strong>Hạn chế chạy nhiều ngày liên tiếp.</strong> Người mới nên xen một ngày nghỉ hoặc đi bộ nhẹ giữa các buổi chạy.</li>
    </ul>

    <h2>Bài khởi động 5–10 phút</h2>
    <p>
      Tăng tốc khi cơ còn lạnh làm tăng nguy cơ chấn thương. Trước khi chạy, khởi động động — vừa vận động vừa mở rộng biên độ khớp — phù hợp hơn
      giãn cơ tĩnh (giữ một tư thế lâu).
    </p>
    <ol>
      <li>Đi bộ nhanh hoặc chạy rất nhẹ 3–5 phút</li>
      <li>Vung chân: vịn tường, mỗi chân vung trước sau 10 lần và sang ngang 10 lần</li>
      <li>Bước chùng chân (lunge) khi đi: 10 bước dài</li>
      <li>Nâng cao đùi và đá gót chạm mông: mỗi động tác 20 giây tại chỗ</li>
      <li>Chạy km đầu tiên của buổi tập chậm hơn mục tiêu</li>
    </ol>

    <h2>Thả lỏng và hồi phục</h2>
    <p>
      Thay vì dừng ngay khi chạy xong, hãy đi bộ hoặc chạy chậm 3–5 phút để hạ nhịp tim, rồi giãn cơ tĩnh bắp chân, mặt trước và sau đùi, cơ mông
      mỗi nhóm 20–30 giây. Ngủ đủ, uống đủ nước và bổ sung protein cùng carbohydrate trong bữa ăn sau khi chạy cũng giúp hồi phục cho buổi chạy tiếp theo.
    </p>

    <h2>Tập sức mạnh 10 phút, 2 lần mỗi tuần</h2>
    <p>
      Chỉ chạy thôi thì các cơ hông, đùi và bắp chân — những cơ hấp thụ lực khi tiếp đất — không đủ khỏe.
      Chỉ cần dành 10 phút vào những ngày không chạy cũng giúp giảm áp lực lên đầu gối và cổ chân.
    </p>
    <ul>
      <li>Squat 15 lần × 3 hiệp</li>
      <li>Glute bridge (nằm ngửa nâng hông) 15 lần × 3 hiệp</li>
      <li>Nhón gót (calf raise) 20 lần × 3 hiệp</li>
      <li>Plank 30 giây × 3 hiệp</li>
    </ul>

    <h2>Khi nào nên thay giày chạy</h2>
    <p>
      Đệm giày giảm dần một cách khó nhận ra. Thường người ta lấy mốc <strong>khoảng 500–800 km</strong> để thay giày, và tùy cân nặng hay kiểu chạy,
      giày có thể mòn nhanh hơn. Nếu đế giày mòn nhiều một bên, hoặc cơn đau chân mới xuất hiện biến mất khi đổi sang giày mới, nghĩa là bạn đã quá thời điểm thay.
      Ghi lại quãng đường đã chạy sẽ giúp ước lượng thời điểm thay dễ hơn.
    </p>

    <h2>Những lúc nên nghỉ</h2>
    <ul>
      <li>Ấn vào một điểm cụ thể thấy đau, và càng chạy càng đau hơn</li>
      <li>Có sưng hoặc bắt đầu đi khập khiễng</li>
      <li>Nghỉ vài ngày mà cơn đau vẫn không giảm</li>
    </ul>
    <p>
      Nghỉ vài ngày hầu như không ảnh hưởng đến thành tích, nhưng cố chạy khi đau đến mức chấn thương nặng hơn có thể khiến bạn phải nghỉ vài tuần đến vài tháng.
    </p>

    <div class="guide-note">
      <p>
        <strong>Trên Pace League,</strong> các buổi chạy được lưu theo ngày nên bạn có thể dễ dàng so sánh tổng quãng đường tuần này với tuần trước.
        Khi tăng quãng đường, hãy kiểm tra mức tăng mỗi tuần trên màn hình lịch sử và lập kế hoạch không quá sức.
      </p>
    </div>
"""

BODY['landit-strategy'] = """
    <p class="guide-lead">
      Landeat là trò chơi chạy bộ của Pace League, nơi lộ trình của bạn chiếm đất trên bản đồ thật. Cùng chạy 5 km, nhưng tùy cách vẽ lộ trình mà
      diện tích chiếm được có thể chênh nhau nhiều lần. Bài viết này tổng hợp luật công nhận đất, cách thiết kế lộ trình để có diện tích lớn nhất
      và mẹo giành đất từ người chạy khác.
    </p>

    <h2>Điều kiện để đất được công nhận</h2>
    <p>Bật chế độ Landeat rồi bắt đầu chạy; nếu thỏa mãn tất cả điều kiện dưới đây, đất sẽ được tạo ngay khi buổi chạy kết thúc.</p>
    <ul>
      <li><strong>Phải quay lại nơi xuất phát.</strong> Điểm kết thúc phải cách điểm xuất phát trong vòng 50 m thì lộ trình mới được tính là một vòng khép kín.</li>
      <li><strong>Chu vi phải từ 300 m trở lên.</strong> Vòng quá ngắn sẽ không được công nhận.</li>
      <li><strong>Diện tích phải từ 10.000 m² (khoảng 100 m × 100 m) đến 5 km².</strong> Vòng quá nhỏ hoặc lớn bất thường sẽ bị loại.</li>
    </ul>
    <p>
      Khi đạt điều kiện, vùng được vòng chạy bao quanh sẽ chuyển thành <strong>các ô lục giác</strong> trên bản đồ. Không chỉ các ô bên trong lộ trình
      mà tất cả các ô lộ trình đi qua cũng thành đất của bạn. Phóng to bản đồ đủ lớn, bạn sẽ thấy trực tiếp lưới lục giác này và có thể kiểm tra
      từng ô xem đất của ai bắt đầu và kết thúc ở đâu.
    </p>

    <h2>Diện tích lớn nhất với cùng quãng đường: hình dạng là tất cả</h2>
    <p>
      Với cùng chu vi, hình càng gần hình tròn thì diện tích bao được càng lớn. Ví dụ với một vòng chạy chu vi 1,2 km:
    </p>
    <div class="guide-table-wrap">
      <table>
        <tr><th>Hình dạng vòng chạy (chu vi 1,2 km)</th><th>Diện tích bao được</th></tr>
        <tr><td>Hình chữ nhật rộng 100 m × dài 500 m</td><td>khoảng 50.000 m²</td></tr>
        <tr><td>Hình vuông 300 m × 300 m (một vòng quanh khu phố)</td><td>khoảng 90.000 m²</td></tr>
        <tr><td>Hình tròn bán kính khoảng 191 m</td><td>khoảng 115.000 m²</td></tr>
      </table>
    </div>
    <p>
      Cùng 1,2 km, vòng dài và hẹp chỉ chiếm được chưa đến một nửa diện tích so với vòng tròn. Trên đường thật không thể chạy thành hình tròn hoàn hảo,
      nên <strong>chạy một vòng quanh khu phố có chiều dài và chiều rộng gần bằng nhau</strong> là lựa chọn thực tế nhất.
      Lộ trình chạy đi rồi quay lại gần như không bao được diện tích nên sẽ không thành đất, hãy lưu ý.
    </p>
    <p>
      Nhân tiện, một vòng đường chạy điền kinh 400 m chỉ bao khoảng 10.000 m², vừa sát mức tối thiểu. Chỉ cần sai số GPS làm diện tích giảm một chút là
      có thể không được công nhận, vì vậy chạy quanh công viên hoặc khu dân cư sẽ chắc chắn hơn đường chạy điền kinh.
    </p>

    <h2>Một vòng lớn hay nhiều vòng nhỏ</h2>
    <p>
      Diện tích tăng theo bình phương chu vi: tăng chu vi gấp đôi thì diện tích tăng khoảng bốn lần. Vì thế một vòng 3 km chiếm được nhiều đất hơn hẳn
      ba vòng 1 km. Nếu đủ sức, một vòng lớn quanh khu bạn sống sẽ có lợi cho bảng xếp hạng diện tích.
      Một vòng vượt quá 5 km² sẽ không được công nhận, nhưng đó là khoảng hình vuông cạnh 2,2 km nên các buổi chạy thông thường gần như không chạm tới.
    </p>

    <h2>Giành đất từ người chạy khác</h2>
    <p>
      Khi vòng chạy mới chồng lên đất của người khác, <strong>các ô lục giác bị chồng lên</strong> sẽ thành của bạn. Luật rất đơn giản:
      <strong>người chạy qua ô đó sau cùng là chủ</strong>. Ai chiếm trước hay giữ bao lâu đều không quan trọng.
    </p>
    <ul>
      <li>Không cần bao trùm toàn bộ đất của đối phương. Chỉ cần chồng một phần, bạn lấy được tất cả các ô bị chồng, còn đất của họ thu nhỏ lại còn các ô còn lại.</li>
      <li>Nếu bao trùm tất cả các ô đất của ai đó, mảnh đất đó sẽ biến mất khỏi bản đồ.</li>
      <li>Chạy lại qua các ô vốn đã là của bạn thì chúng giữ nguyên; chỉ các ô trống mới phủ và các ô giành được mới được gộp thành đất mới.</li>
    </ul>
    <p>
      Ngược lại, đất của bạn cũng có thể bị lấy bất cứ lúc nào. Nếu có người khác hoạt động quanh cung đường quen thuộc của bạn, hãy định kỳ chạy lại
      vòng đó để lấy lại các ô bị mất. Bảng xếp hạng Landeat trên màn hình bản đồ cho biết tổng diện tích của từng người, nên xem đất của những người
      dẫn đầu nằm ở đâu rồi lên kế hoạch lộ trình cũng là một cách hay.
    </p>

    <h2>Danh sách kiểm tra để không thất bại</h2>
    <ol>
      <li>Trước khi chạy, kiểm tra chế độ Landeat đã bật chưa.</li>
      <li>Ghi nhớ điểm xuất phát và luôn nhấn kết thúc trong phạm vi 50 m quanh điểm đó.</li>
      <li>Tránh để phần lớn vòng chạy đi qua khu vực GPS yếu như giữa các tòa nhà cao tầng hay trong đường hầm.</li>
      <li>Chọn lộ trình đi vòng theo một hướng thay vì chạy đi rồi quay lại.</li>
    </ol>

    <div class="guide-note">
      <p>
        <strong>An toàn là trên hết.</strong> Đừng băng qua lòng đường, vào khu đất tư nhân hay công trường cấm vào, hoặc chạy ở nơi vắng vẻ lúc đêm khuya
        chỉ để chiếm thêm đất. Mọi mảnh đất đều có thể chiếm được chỉ bằng đường phố và vỉa hè công cộng.
      </p>
    </div>
"""


# ---------------------------------------------------------------- 소개 페이지 (/about)
ABOUT = {'title': 'Giới thiệu', 'desc': 'Pace League là dịch vụ chạy bộ tích hợp ghi lại buổi chạy kèm tính pace và calo, bảng xếp hạng và hạng, trò chơi chiếm đất Landeat, crew chạy bộ và cộng đồng trong cùng một nơi.', 'h1': 'Pace League — dịch vụ chạy bộ cho bạn lý do để chạy mỗi ngày', 'lead': 'Pace League không chỉ là ứng dụng ghi lại quãng đường bạn đã chạy. Mỗi buổi chạy được quy đổi thành điểm và hạng để so tài, lộ trình GPS giúp bạn chiếm đất trên bản đồ thật, bạn có thể lập crew để chạy cùng nhau và chia sẻ thành tích, câu chuyện với những người chạy khác — tất cả nhằm biến “chạy một mình” thành “chạy cùng nhau và chạy đều đặn”.', 'sections': [('clock', 'Ghi lại buổi chạy, tự động tính pace và calo', 'Khi bắt đầu chạy trên ứng dụng, tọa độ GPS được gửi lên máy chủ theo thời gian thực và lưu thành một buổi chạy. Khi kết thúc, pace trung bình (phút/km) và lượng calo tiêu hao được tự động tính từ quãng đường và thời gian rồi lưu vào lịch sử chạy của bạn. Không cần máy tính hay nhập tay — chỉ cần chạy là thành tích tự tích lũy.'), ('trophy', 'Bảng xếp hạng và hệ thống hạng', 'Các buổi chạy được quy đổi thành điểm phản ánh quãng đường và pace, và mỗi mùa, điểm này xếp bạn vào một hạng từ Đồng đến hạng cao nhất. Ngoài bảng xếp hạng toàn mùa, bảng TOP 10 ngay trên trang chủ giúp bạn nhìn thấy ngay mình đang ở đâu so với những người chạy khác.'), ('pin', 'Landeat — trò chơi chạy bộ chiếm đất', 'Landeat là trò chơi chạy bộ dựa trên bản đồ của riêng Pace League. Chạy một vòng khép kín đủ quãng đường tối thiểu và quay lại điểm xuất phát, vùng mà lộ trình bao quanh sẽ chuyển thành các ô lục giác (đất) trên bản đồ thật và thuộc về bạn. Nếu lộ trình chồng lên đất người khác đã chiếm, bạn có thể giành lấy phần đó, nên dù chạy cùng một khu phố, mỗi lần vẫn cần chiến thuật khác nhau. Trên màn hình bản đồ, bạn xem được đất của mình, đất của người khác và bảng xếp hạng Landeat theo tổng diện tích.'), ('users', 'Crew — đội chạy cùng nhau', 'Nếu muốn chạy theo đội thay vì một mình, bạn có thể lập crew hoặc tham gia crew có sẵn. Mỗi người chỉ thuộc một crew, do trưởng crew điều hành bằng cách mời người chạy khác hoặc duyệt yêu cầu tham gia. Khi đã tham gia, huy hiệu crew sẽ hiện bên cạnh bài viết và trên bảng xếp hạng, để mọi người dễ dàng biết bạn thuộc crew nào.'), ('chat', 'Cộng đồng', 'Cộng đồng chia thành các bảng trò chuyện tự do, hỏi đáp, xác nhận buổi chạy và giới thiệu crew, nơi bạn chia sẻ thành tích, chiến thuật Landeat và tin tức crew. Bạn có thể đính kèm ảnh và video ngay trong trình soạn thảo, và gắn buổi chạy của mình vào bài viết để xác nhận. Ai cũng có thể xem danh sách bài và đọc bài viết mà không cần đăng nhập; chỉ viết bài, bình luận và bình chọn mới cần tài khoản.')], 'cta': 'Hãy ghi lại buổi chạy đầu tiên ngay hôm nay.', 'join': 'Đăng ký', 'login': 'Đăng nhập', 'links': {'guide': 'Hướng dẫn chạy bộ', 'privacy': 'Chính sách quyền riêng tư', 'terms': 'Điều khoản dịch vụ', 'location': 'Điều khoản dịch vụ dựa trên vị trí'}}
