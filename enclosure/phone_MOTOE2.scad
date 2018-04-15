/* MOTO E2
 *
 * Max: [130, 67, 12.5]
 * About 1-1.5mm curvature on top and bottom sides
 * Distance middle usb port to edge: 33.5
 * length usb port: 8.1 (long side of port facing down)
 * corner radius: 10mm
 * curvature bottom: 12.5-8.5
 * button positions from bottom: 65 [17] 10 [9] 29
 * camera diameter: 13, camera center distance from top: 23
 *
 */


lensHole = [sizeBot[0]-30, sizeBot[1]/2];

module phone() {
    translate([]) {
        block(130, 67, 12.5, crad=10);
        color("grey") {
            translate([65, -0.5, 7]) cube([17, 2, 1.5]);
            translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
            translate([130-22, 67/2, -1]) cylinder($fn=32, h=3, d=13);
        }
    }
}

module cavity(height) {
    translate([sizeBot[0]-141, 10, 7]) {
        difference() {
            translate([-10, 0]) block(143, 67.75, height, crad=8);
        
    //        points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
    //        translate([-12, 68-.1, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
            
            translate([-10, 67.75-1.5, 12.5]) rotate([0, 90, 0]) linear_extrude(height=130+15) polygon([[0, 0], [4, 1.5], [0, 1.5]]);
        }
        
        // buttons
        translate([65, -3.9, 0]) linear_extrude(height=height) polygon([[3, 1], [41-12, 1], [41-11, 0], [41, 0], [41, 4], [0, 4]]);//cube([41, 5, height]);
        
        // wedge
        translate([16, -5, 0]) cube([30.5, 2.3, height]);
        translate([16+2, -5, 0]) cube([26.5, 6, height]);   
        
        translate([06, 10.5, -0.3]) rotate([0, 0, 0]) linear_extrude(height=10) color("DarkRed") text("MOTO E2");
    }
}