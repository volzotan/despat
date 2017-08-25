sizeBot    = [155, 84, 20];
sizeTop    = [155, 84, 25];
lensHole   = [127, 80/2];

lidDepth   = 1;
lidWall    = 0.8;
lidTol     = 0.4;

crad       = 6;
w          = 2.0;
wb         = 1.2;

bottom();
translate([0, 90, 0]) top();
translate([sizeBot[0]/2-(44/2), -.1, 4]) rotate([90, 0, 0]) socket();
% translate([sizeBot[0]/2-(44/2), -30, 4]) rotate([0, 0, 0]) socket();
% translate([63.5, -5-2, 30]) rotate([-90, 0, 0]) DIN912screw(8);

% translate([20, 6.5, 5+0]) phone();

translate([127, 40, -10]) uvfilter();

// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

module socket() {
    width = 44;
    depth = 16;
    difference() {
        block(width, depth, 7);
        
        translate([(width)/2-14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2+14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2-14, depth/2, 1.8]) cylinder($fn=32, h=20, d=9);
        translate([(width)/2+14, depth/2, 1.8]) cylinder($fn=32, h=20, d=9);
        
        translate([(width)/2, depth/2, -10]) cylinder($fn=32, h=20, d=7);
        translate([(width)/2, depth/2, -1]) cylinder($fn=6, h=1+6, d=13.15);  // +1 safety margin for long fastening screws
    }
}

module top() {
    bottomRounding = 3;
    bottomReduction = 2;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=bottomReduction);
                    translate([0, 0, bottomRounding]) 
                        block(sizeTop[0], sizeTop[1], sizeTop[2]-bottomRounding, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=bottomReduction+w);
                    translate([0, 0, bottomRounding]) 
                        block(sizeTop[0], sizeTop[1], sizeTop[2]-bottomRounding, crad=crad, red=w);
                }
                
                translate([0, 0, sizeTop[2]-lidDepth]) difference() {
                    block(sizeTop[0], sizeTop[1], sizeTop[2]-bottomRounding, crad=crad, red=-1);
                    block(sizeTop[0], sizeTop[1], sizeTop[2]-bottomRounding, crad=crad, red=lidWall+lidTol);
                }
            }
        }
    }
    
    // translate([34, 3, 8]) 
    translate([sizeTop[0]-3, 55, 8]) rotate([0, -90, 0]) import("dht22.stl");
    
    // battery holder
    % translate([60, 3, 21]) cube([79, 78, 21]);
    
    // batteries
    % translate([0, 0, 40]) {
        translate([16, 5, 5]) cube([25.4, 65, 1.2]);
        translate([54+20*0, 70, 14]) rotate([90, 0, 0]) cylinder($fn=32, h=65, d=18);
        translate([54+20*1, 70, 14]) rotate([90, 0, 0]) cylinder($fn=32, h=65, d=18);
        translate([54+20*2, 70, 14]) rotate([90, 0, 0]) cylinder($fn=32, h=65, d=18);
        translate([54+20*3, 70, 14]) rotate([90, 0, 0]) cylinder($fn=32, h=65, d=18);
    }
}

module bottom() {
    bottomRounding = 3;
    bottomReduction = 1;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction);
                    translate([0, 0, bottomRounding]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomRounding, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction+w);
                    translate([0, 0, bottomRounding]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomRounding, crad=crad, red=w);
                }
                
                translate([0, 0, sizeBot[2]-lidDepth]) block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomRounding, crad=crad, red=lidWall);
            }
            
            // camera lens reinforcement
            translate([lensHole[0], lensHole[1], wb]) hull() {
                cylinder($fn=64, h=0.1, d=37+5);
                translate([0, 0, 1.2]) cylinder($fn=64, h=0.1, d=37+3);
            }
        }
        
        // camera lens
        translate([lensHole[0], lensHole[1], -1]) {
            translate([]) cylinder($fn=64, h=10, d=37);
            translate([0, 0, 1+wb]) cylinder($fn=64, h=10, d=38);
        }
        
        // socket screw holes
        translate([sizeBot[0]/2, 10-1, 12]) rotate([90, 0, 0]) {
            translate([-14, 0, 0]) cylinder($fn=32, h=10, d=5.3);
            translate([+14, 0, 0]) cylinder($fn=32, h=10, d=5.3);
        }
    }
    
    // nutholder
    difference() {
        intersection() {
            union() {
                cube([14, 14, 19]);
            }
            hull() {
                block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction);
                translate([0, 0, bottomRounding]) block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomRounding, crad=crad);
            }
        }
        
        translate([7, 7, 10]) cylinder($fn=32, d=5.3, h=30);
    }
    
    // nuts
    
    % translate([sizeBot[0]/2, w+4+0.2, 12]) rotate([90, 0, 0]) { 
        translate([-14, 0, 0]) M5nut();
        translate([+14, 0, 0]) M5nut();
    }
}

// *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***

module uvfilter() {
    color("grey") {
        cylinder($fn=64, h=3.6, d=39.10);
        cylinder($fn=64, h=5.6, d=36.90);
    }
}

module M5nut() {
    color("grey") difference() {
        cylinder($fn=6, h=4, d=9.5);
        translate([0, 0, -1]) cylinder($fn=32, d=5, h=6);
    }
}

module DIN912screw(length) {
    difference() {
        union() {
            cylinder($fn=32, h=5, d=8.5);
            translate([0, 0, 5]) cylinder($fn=32, h=length, d=5);
        }
        translate([0, 0, -1]) cylinder($fn=6, h=3, d=5);
    }
}

module phone() {
    //cube([154, 75, 10]); // Moto Z
    block(130, 67, 12.3, crad=10);
    translate([65, -0.5, 7]) cube([17, 2, 1.5]);
    translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
    translate([130-23, 67/2, -1]) cylinder($fn=32, h=3, d=13);
}

module block(width, depth, height, crad=3, red=0) {
    hull() {    
        translate([crad+red, crad+red]) cylinder($fn=32, h=height, r=crad);
        translate([width-crad-red, crad+red]) cylinder($fn=32, h=height, r=crad);
        translate([crad+red, depth-crad-red]) cylinder($fn=32, h=height, r=crad);
        translate([width-crad-red, depth-crad-red]) cylinder($fn=32, h=height, r=crad);
    }
}

// Phone Moto E Measurement

// Max: [130, 67, 12.5]
// About 1-1.5mm curvature on top and bottom sides
// Distance middle usb port to edge: 33.5
// length usb port: 8.1 (long side of port facing down)
// corner radius: 10mm
// curvature bottom: 12.5-8.5
// button positions from bottom: 65 [17] 10 [9] 29
// camera diameter: 13, camera center distance from top: 23