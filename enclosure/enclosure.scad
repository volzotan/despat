sizeBot    = [120, 60, 20];
sizeTop    = [120, 60, 40];

crad       = 8;
w          = 2.0;
wb         = 1.2;

bottom();

// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

module bottom() {
    bottomRounding = 3;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=5);
                    translate([0, 0, bottomRounding]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomRounding, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=5+w);
                    translate([0, 0, bottomRounding]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomRounding, crad=crad, red=w);
                }
            }
            
            // camera lens reinforcement
            translate([80, 30, wb]) hull() {
                cylinder($fn=32, h=0.1, d=14);
                translate([0, 0, 1.2]) cylinder($fn=32, h=0.1, d=12);
            }
        }
        
        // camera lens
        translate([80, 30, -1]) {
            translate([]) cylinder($fn=32, h=10, d=10);
            translate([0, 0, 1+wb]) cylinder($fn=32, h=10, d=11);
        }
        
        // socket screw holes
        translate([sizeBot[0]/2, 10-1, 12]) rotate([90, 0, 0]) {
            translate([-12, 0, 0]) cylinder($fn=32, h=10, d=5.3);
            translate([12, 0, 0]) cylinder($fn=32, h=10, d=5.3);
        }
    }
}










module block(width, depth, height, crad=3, red=0) {
    hull() {    
        translate([crad+red, crad+red]) cylinder($fn=32, h=height, r=crad);
        translate([width-crad-red, crad+red]) cylinder($fn=32, h=height, r=crad);
        translate([crad+red, depth-crad-red]) cylinder($fn=32, h=height, r=crad);
        translate([width-crad-red, depth-crad-red]) cylinder($fn=32, h=height, r=crad);
    }
}