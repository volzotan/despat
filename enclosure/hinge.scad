//hinge_bottom();

module hinge_bottom(screwed=false) {
    diam        = 10;
    hingeTol    = 0.3;
    
    difference() { 
        union() {
            translate([0, 0, -2]) cylinder($fn=64, d=diam, h=3+2);
            translate([0, 0, 3*2 + hingeTol*2]) cylinder($fn=64, d=diam, h=3);
            translate([0, 0, 3*4 + hingeTol*4]) cylinder($fn=64, d=diam, h=3+2);
              
            color("red") translate([0, 0, -2]) linear_extrude(height=2*2 + 5*3 + hingeTol*4) polygon(points);
            points = [[0, diam/2], [3.54, -3.54], [13.5, 5.5], [0.5, 5.5]];

            if (screwed) {
                intersection() {
                    hull() {
                        translate([8.5, 5.5, -5]) rotate([90, 0, 0]) cylinder($fn=32, d=10, h=10);
                        //translate([3, -4.5, -5.5]) cube([10, 10, 10]);
                        translate([8.5, 5.5, +21]) rotate([90, 0, 0]) cylinder($fn=32, d=10, h=10);
                    }

                    color("red") translate([0, 0, -12]) linear_extrude(height=50) polygon(points);
                }
            }
        }
        
        translate([0, 0, 3]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        translate([0, 0, 3*3+hingeTol*2]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        
        translate([0, 0, -1]) cylinder($fn=32, h=30, d=3.5); // diameter slightly increased
        translate([0, 0, -5]) cylinder($fn=32, h=5, d=6);
        translate([0, 0, 16]) cylinder($fn=6, h=5, d=6.6);
        
        translate([0, -6, 3]) cube([20, 7.5, 3+hingeTol*2]);
        translate([0, -6, 3*3+hingeTol*2]) cube([20, 7.5, 3+hingeTol*2]);
        
        if (screwed) {
            // translate([9, 10, 8.075]) 
            translate([8.5, 10, -5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=20);
            // translate([9, 4.3, 8.075]) 
            translate([8.5, 3.5, -5]) rotate([90, 0, 0]) cylinder($fn=32, d=6, h=20);

            translate([8.5, 10, 21]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=20);
            translate([8.5, 3.5, 21]) rotate([90, 0, 0]) cylinder($fn=32, d=6, h=20);
        }
    }
    
    % translate([0, 0, -3.2]) DIN912screw(20);
}

module hinge_top() {
    diam        = 10;
    hingeTol    = 0.3;
    
    difference() { 
        union() {
            translate([0, 0, 3 + hingeTol]) cylinder($fn=32, d=diam, h=3);
            translate([0, 0, 3*3 + hingeTol*3]) cylinder($fn=32, d=diam, h=3);
            
            color("green") translate([0, 0, 3+hingeTol]) linear_extrude(height=3*3 + hingeTol*2) polygon(points);
            points = [[0, diam/2], [-3.54, -3.54], [-13, 5.5], [-0.5, 5.5]];  
        }
        
        translate([0, 0, 3*2+hingeTol]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        
        translate([0, 0, -1]) cylinder($fn=32, h=30, d=3.5); // diameter slightly increased
        translate([0, 0, -5]) cylinder($fn=32, h=5, d=6);
        
        translate([-10, -6, 6+hingeTol]) cube([10, 7.5, 3+hingeTol*2]);
    }
    
    % translate([0, 0, -3.2]) DIN912screw(20);
}