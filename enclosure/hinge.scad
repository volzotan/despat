// --- TEST

////intersection() {
//hinge_bottom(screwed=true);
////    translate([-30, 0, -20]) cube([100, 100, 100]);
////}
//hinge_top();

// --- PRINT

//translate([0, 0, 5.5]) rotate([-90, 0, 0]) hinge_bottom(screwed=true);
//translate([-12, 0, 5.5]) rotate([-90, 0, 0]) mirror([1, 0]) hinge_bottom(screwed=true);

module hinge_bottom(screwed=false) {
    diam        = 10;
    hingeTol    = 0.3;
    
    red         = 0.0;
    
    z_offset    = 0.5;
    
    difference() { 
        union() {
            translate([-z_offset, 0, -5]) cylinder($fn=64, d=diam, h=4+2);
            translate([-z_offset, 0, -1+3*2 + hingeTol*2]) cylinder($fn=64, d=diam, h=5);
            translate([-z_offset, 0, 3*4 + 2 + hingeTol*4]) cylinder($fn=64, d=diam, h=4+2);
              
            points = [[red-z_offset-1, diam/2], [3.54-z_offset, -3.54], [13.5, 5.5], [0.5+red-z_offset-1, 5.5]];
            color("red") translate([0, 0, -5]) linear_extrude(height=2*2 + 5*3 + 6 + hingeTol*4) polygon(points);

            if (screwed) {
                points_r1 = [[1-z_offset, -4.9], [13.5, -3.5], [13.5, 5.7], [0, 5.7]];
                intersection() {
                    translate([0, 0, -9]) color("purple") linear_extrude(height=34) polygon(points_r1);
                    
                    union() {
                        translate([-z_offset, -15, -5]) cube([10, 20, 26+.2]);
                        hull() {
                            crad = 3;
                            translate([8.5+4, 5.5, -5-crad]) rotate([90, 0, 0]) cylinder($fn=32, d=2, h=20);
                            translate([8.5+4, 5.5, +21+crad]) rotate([90, 0, 0]) cylinder($fn=32, d=2, h=20);
                            translate([8.5-4, 5.5, -5-crad]) rotate([90, 0, 0]) cylinder($fn=32, d=2, h=20);
                            translate([8.5-4, 5.5, +21+crad]) rotate([90, 0, 0]) cylinder($fn=32, d=2, h=20);
                        }
                    }
                }
            }
        }
        
        translate([0, 0, 1]) cylinder($fn=32, d=diam+2, h=4+hingeTol*2);
        translate([0, 0, 1+3*3+hingeTol*2]) cylinder($fn=32, d=diam+2, h=4+hingeTol*2);
        
        translate([0, 0, 1]) cube([3.5, 10, 4+hingeTol*2]);
        translate([0, 0, 1+3*3+hingeTol*2]) cube([3.5, 10, 4+hingeTol*2]);
        
        translate([-z_offset, 0, -10]) cylinder($fn=32, h=50, d=3.5); // diameter slightly increased
        translate([-z_offset, 0, -8]) cylinder($fn=32, h=5, d=6);
        translate([-z_offset, 0, 19]) rotate([0, 0, 30]) cylinder($fn=6, h=5, d=6.6+0.1);
        
        
        points_c = [[0, 0], [14, 0], [13, 1], [10, 7.5], [0, 7.5]];
        translate([0, -6, 1]) linear_extrude(height=4+hingeTol*2) polygon(points_c);
        translate([0, -6, 1+3*3+hingeTol*2]) linear_extrude(height=4+hingeTol*2) polygon(points_c);

        if (screwed) {
            // translate([9, 10, 8.075]) 
            translate([8.5, 10, -5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=20);
            // translate([9, 4.3, 8.075]) 
            translate([8.5, 2, -5]) rotate([90, 0, 0]) cylinder($fn=32, d=6, h=20);

            translate([8.5, 10, 21]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=20);
            translate([8.5, 2, 21]) rotate([90, 0, 0]) cylinder($fn=32, d=6, h=20);
        }
    }
    
    // % translate([0, 0, -3.2-3]) DIN912screw(25);
}

module hinge_top() {
    diam        = 10;
    hingeTol    = 0.3;
    
    red         = 0.0;
    
    difference() { 
        union() {
            translate([0, 0, 1 + hingeTol]) cylinder($fn=64, d=diam, h=4);
            translate([0, 0, 1+3*3 + hingeTol*3]) cylinder($fn=64, d=diam, h=4);
            
            points = [[-red, diam/2], [-3.54, -3.54], [-13, 5.5], [-0.5-red, 5.5]]; 
            color("green") translate([0, 0, 1+hingeTol]) linear_extrude(height=4+3*3 + hingeTol*2) polygon(points); 
        }
        
        translate([0, 0, -1+3*2+hingeTol]) cylinder($fn=32, d=diam+2, h=5+hingeTol*2);
        
        translate([0, 0, -1]) cylinder($fn=32, h=30, d=3.5); // diameter slightly increased
        translate([0, 0, -5]) cylinder($fn=32, h=5, d=6);
        
        translate([-10, -6, 5+hingeTol]) cube([10, 7.5, 5+hingeTol*2]);
    }
}

module DIN912screw(length) {
    % difference() {
        union() {
            cylinder($fn=32, h=3, d=5.5);
            translate([0, 0, 3]) cylinder($fn=32, h=length, d=3);
        }
        translate([0, 0, -1]) cylinder($fn=6, h=3, d=2);
    }
}