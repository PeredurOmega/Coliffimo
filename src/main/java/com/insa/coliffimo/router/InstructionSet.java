package com.insa.coliffimo.router;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;
import com.graphhopper.util.InstructionList;

public class InstructionSet {

    public final TourActivities tourActivities;
    public final InstructionList fullInstructions;

    public InstructionSet(TourActivities tourActivities, InstructionList fullInstructions) {
        this.tourActivities = tourActivities;
        this.fullInstructions = fullInstructions;
    }
}
