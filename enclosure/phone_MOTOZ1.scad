/* MOTO Z1
 *
 */


lensHole = [sizeBot[0]-29, sizeBot[1]/2]; 

module phone() {
    color("green") block(155, 75.3, 12, crad=10);
    
    translate([155-23, 75.3/2, -1]) color("red") cylinder($fn=32, d=9, h=3);
    translate([155-20-6*0-6.3*1, -1, 12-3.5]) cube([6.3, 3, 1]);
    translate([155-20-6*1-6.3*2, -1, 12-3.5]) cube([6.3, 3, 1]);
    translate([155-20-6*2-6.3*3, -1, 12-3.5]) cube([6.3, 3, 1]);
}

module cavity(height) {
    size = [155, 75.3+0.6];
    
    translate([sizeBot[0]-160.25, 9, 7]) {
        difference() {
            union() {
                block(size[0], size[1], height, crad=08);
                
                // buttons
                translate([size[0]-53, -3, 1]) block(34.5, 5, height, crad=1);
            }
                    
            points_ridge = [[0, 0], [0, 10], [1, 10+2], [1, height], [0, height]];
            translate([size[0], size[1]]) rotate([90, 0, -90]) linear_extrude(height=size[0]) polygon(points_ridge);
            
            points_valley = [[0, 1], [10, 0], [size[1]-10, 0], [size[1], 1], [size[1], -10], [0, -10]];
            translate([size[0], size[1]]) rotate([90, 0, -90]) linear_extrude(height=size[0]) polygon(points_valley);
        }
        
        // text
        translate([24, 11, -0.6]) rotate([0, 0, 0]) linear_extrude(height=10) color("DarkRed") text("MOTO Z1");

        // wedge nudge
        difference() {
            union() {
                translate([38, -10-1, 0]) cube([30, 10, 20]);
                translate([38, -10+1, 0+1]) cube([30, 10, 20]);
            }
            translate([38, -3.5, 5]) rotate([90, 0, 90]) cylinder($fn=32, d1=3, d=2, h=0.5);
            translate([38+30, -3.5, 5]) rotate([90, 0, -90]) cylinder($fn=32, d1=3, d=2, h=0.5);
        }
    }
}