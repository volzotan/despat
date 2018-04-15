/* NEXUS 5
 *
 */


lensHole = [sizeBot[0]-25, 25];

module phone() {
    translate([-5, 68, -26]) rotate([0, 0, -90]) import("nexus5.stl");
}

module cavity(height) {
    translate([30, 12, 7]) {
        difference() {
            union() {
                color("red") {
                    translate([125, 62+1, 0]) cylinder($fn=32, h=height, d=12);
                    translate([125, 4,  0]) cylinder($fn=32, h=height, d=12);
                }
                color("green") intersection() {
                    translate([120, 3, 0]) cube([20, 60+1, height+2]);
                    translate([125.0, (56+1)/2+5,  0]) scale([0.18, 1]) cylinder($fn=32, h=height, d=90);
                }
                
                translate([-11, -2, 0]) cube([136, (56+1)+14, height]);
            }
        
            points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
            translate([-12, 68-.1, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
        }
        
        translate([5, 10.5, -0.6]) linear_extrude(height=10) color("DarkRed") text("NEXUS 5");
        
        translate([102, -4.9, 0])cube([16, 5, height]);
        translate([82, 65, 0])cube([26, 5, height]);
        
        // wedge
        translate([40, -5.5, 0]) cube([40, 2, height]);
        translate([40+2, -5.5, 0]) cube([36, 5, height]);
    }
}