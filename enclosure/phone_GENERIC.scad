/* PHONE GENERIC
 * 
 * this is a template for a generic phone.
 * It can be used as base to add new phones.
 *
 */

phoneDepth = 70;
phoneWidth = 140;
phoneHeight = 10;
lensHoleDistance = 30;
phoneName = "GENERIC";


lensHole = [sizeBot[0]-lensHoleDistance, sizeBot[1]/2];

module phone() {
    translate([]) {
        block(phoneWidth, phoneDepth, phoneHeight, crad=10);
    }
}

module cavity(height) {
    translate([sizeBot[0]-(phoneWidth-1), 10, 7]) {
        difference() {
            translate([-10, 0]) block(phoneWidth, phoneDepth, height, crad=8);
        }
        
        // buttons
        translate([70, -4, 0]) block(30, 10, height, crad=2);
        
        // wedge
        translate([16, -10, 0]) cube([30, 11, height]);  
        
        translate([06, 10.5, -0.3]) rotate([0, 0, 0]) linear_extrude(height=10) color("DarkRed") text(phoneName);
    }
}