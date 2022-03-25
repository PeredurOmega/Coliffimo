package com.insa.coliffimo;

import com.insa.coliffimo.business.Intersection;
import com.insa.coliffimo.business.Map;
import com.insa.coliffimo.utils.XmlParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class TestMap {
    @Test
    @DisplayName("Test_find_closest_intersection_with_coordinates")
    void Test_Find_closest_intersection_with_coordinates() {
        Map map = new Map();
        map.addIntersection(new Intersection(21992964L, (float)45.74778, (float)4.8682485));
        map.addIntersection(new Intersection(208769133L, (float)45.759453, (float)4.8698664));
        map.addIntersection(new Intersection(342873532L, (float)45.76051, (float)4.8783274));
        map.addIntersection(new Intersection(208769499L, (float)45.760597, (float)4.87622));
        map.addIntersection(new Intersection(975886496L, (float)45.756874, (float)4.8574047));

        Intersection res = map.findClosestIntersection((float) 45.760597, (float) 4.87622);
        Assertions.assertEquals(res, map.getListIntersections().get(208769499L));
    }
}