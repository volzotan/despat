/* ZTE AXON 7
 *
 */


lensHole = [sizeBot[0]-29.5, 47.5];

module phone() {
    translate([07, 9.9, 8]) {
        block(151.6, 74.8, 9, crad=12-5);
        color("grey") {
            translate([151.6-31.5-24, -0.5, 4.5]) cube([24, 2, 1.5]);
            translate([151.6-74, -0.5, 4.5]) cube([11, 2, 1.5]);
    //        translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
            translate([151.6-21, 74.8/2, -1]) cylinder($fn=32, h=3, d=13);
        }
    }
}

module cavity(height) {
    
    size = [151.6+1, 74.8+0.6+2];

    translate([sizeBot[0]-160.25, 10, 7]) {
        difference() {
            union() {
                translate([-0.5, -0.2, 0]) block(size[0], size[1], height, crad=6);
            }
        
            points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
            translate([-1, size[1]-1.7, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
        }
        
        translate([-10, -0.5]) cube([15, size[1]-1-2, height]);
        
        // text
        translate([27, 10.5, -0.6]) rotate([0, 0, 0]) linear_extrude(height=10) color("DarkRed") text("ZTE AXON 7");
        
        // buttons
        translate([75, -4, 0]) cube([48, 5, height]);
        
        // wedge nudge
        translate([40, -10-1, -1]) cube([30, 10, 20]);
        translate([40, -10+1, 0]) cube([30, 10, 20]);
    }
}
