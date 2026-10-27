package com.paceleague.record.application.service;

import com.paceleague.record.application.dto.CoursePoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecordCourseDownsampleTest {

    private List<CoursePoint> points(int n) {
        List<CoursePoint> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(new CoursePoint(i, i));
        return list;
    }

    @Test
    void 최대_개수_이하면_그대로() {
        List<CoursePoint> pts = points(RecordQueryService.COURSE_MAX_POINTS);

        assertThat(RecordQueryService.downsample(pts)).isSameAs(pts);
    }

    @Test
    void 많으면_최대_개수로_솎되_시작과_끝은_유지한다() {
        List<CoursePoint> pts = points(2500);

        List<CoursePoint> result = RecordQueryService.downsample(pts);

        assertThat(result).hasSize(RecordQueryService.COURSE_MAX_POINTS);
        assertThat(result.get(0)).isEqualTo(pts.get(0));
        assertThat(result.get(result.size() - 1)).isEqualTo(pts.get(2499));
    }
}
