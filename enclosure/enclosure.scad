include <hinge.scad>

sizeBot    = [160, 86, 20];
sizeTop    = [160, 86, 25];
lensHole   = [sizeBot[0]-25, 25];

lidDepth   = 1;
lidWall    = 0.8;
lidTol     = 0.4;

crad       = 6;
w          = 2.4+.1;
wb         = 1.2;

bottom();
translate([0, 100-3, 0]) top();
translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 4]) rotate([90, 0, 180]) socket();
% translate([sizeBot[0]/2-(44/2), -30, 4]) rotate([0, 0, 0]) socket();
% translate([63.5, -5-2, 30]) rotate([-90, 0, 0]) DIN912screw(8);

//% translate([23, 10, 5+0]) nexus5();
//% translate([20, 10, 5+0+50]) nexus5();

* translate([127, 40, -10]) uvfilter();

translate([200, 0, 0]) {
    bottom();
    translate([0, sizeTop[1], 44.1]) rotate([180, 0, 0]) top();
    translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 4]) rotate([90, 0, 180]) socket();
    translate([55, 0, 44]) rotate([0, 180, 0]) color("green") import("hinge.stl");
    translate([95+55, 0, 44]) rotate([0, 180, 0]) color("green") import("hinge.stl");
}

//translate([30, 105, 4]) cube([126, 69, 14]); // jet
translate([30, 105, 4]) cube([92, 59, 22]); // anker 10.000
//translate([30, 105, 4]) cube([98, 8, 22]); // anker 10.000


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
                
                * translate([0, 0, sizeTop[2]-lidDepth]) difference() {
                    block(sizeTop[0], sizeTop[1], sizeTop[2]-bottomRounding, crad=crad, red=-1);
                    block(sizeTop[0], sizeTop[1], sizeTop[2]-bottomRounding, crad=crad, red=lidWall+lidTol);
                }
            }
            
            translate([0, 0, sizeTop[2]-.1]) color("red") difference() {
                height = 1;
                block(sizeTop[0], sizeTop[1], height, crad=crad, red=0.8);
                translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height+2, crad=crad, red=0.8+0.8);
            }
        }
        
        // hinge holes
        translate([30, sizeBot[1]+5, 17.6]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
        translate([sizeBot[0]-30, sizeBot[1]+5, 17.6]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
    }
    
    translate([+20+18.2, -5.5, 24]) rotate([0, -90, 0]) hinge_top();
    translate([-20+sizeTop[0]-2, -5.5, 24]) rotate([0, -90, 0]) hinge_top();
    
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
    bottomHeight = 3;
    bottomReduction = 2;
    topHeight = 3;
    topReduction = 1;
    topSleeveHeight = 1;
    middlePartHeight = sizeBot[2] - bottomHeight - topHeight - topSleeveHeight - wb;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction);
                    translate([0, 0, bottomHeight]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomHeight, crad=crad);
                }
                                
                union() {
                    translate([0, 0, wb]) hull() {
                        block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction+w);
                        translate([0, 0, bottomHeight]) 
                            block(sizeBot[0], sizeBot[1], middlePartHeight, crad=crad, red=w);
                        translate([0, 0, bottomHeight+middlePartHeight+topHeight]) 
                            block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=topReduction+w);
                    }
                    
                    translate([0, 0, sizeBot[2]-topSleeveHeight-0.1]) 
                            block(sizeBot[0], sizeBot[1], topSleeveHeight+0.2, crad=crad, red=topReduction+w);
                }
                
                translate([0, 0, sizeBot[2]-.6]) color("red") difference() {
                    height = 1;
                    block(sizeBot[0], sizeBot[1], height, crad=crad, red=0.8);
                    translate([0, 0, -1]) block(sizeBot[0], sizeBot[1], height+2, crad=crad, red=0.8+0.8);
                }
            }
            
            // camera lens reinforcement
            * translate([lensHole[0], lensHole[1], wb]) hull() {
                cylinder($fn=64, h=0.1, d=37+5);
                translate([0, 0, 1.2]) cylinder($fn=64, h=0.1, d=37+3);
            }
            
            // inlay
            translate([0, 0, 0]) intersection() {
                difference() {
                    translate([23, w, 0]) cube([sizeBot[0]-20, sizeBot[1]-2*w, 14]); //20-1]);
                    translate([23, 10, 6]) color("red") nexus5cavity(10);
                    * hull() {
                        translate([23, 9, 15-.1]) color("red") nexus5cavity(0.1);
                        translate([23-1, w, 20-1]) cube([sizeBot[0]-19, sizeBot[1]-2*w, 1]);
                    }
                }
                hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction);
                    translate([0, 0, bottomHeight]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomHeight, crad=crad);
                }
            }
        }
        
        // camera lens
        translate([lensHole[0], lensHole[1], -1]) {
            cylinder($fn=64, h=1+3.60+0.3, d=38);
            translate([0, 0, 1+3.60+0.3-0.1]) cylinder($fn=64, h=1, d1=38, d=37);
            cylinder($fn=64, h=10, d=37);
        }
        
        // socket screw holes
        translate([sizeBot[0]/2, sizeBot[1]+5, 12]) rotate([90, 0, 0]) {
            translate([-14, 0, 0]) cylinder($fn=32, h=10, d=5.3);
            translate([+14, 0, 0]) cylinder($fn=32, h=10, d=5.3);
        }
        
        // hinge holes
        translate([30, 5, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
        translate([sizeBot[0]-30, 5, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
        translate([30, 10+2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        translate([sizeBot[0]-30, 10+2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        
        // socket holes
        translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 12]) rotate([90, 0, 0]) { 
            translate([-14, 0, 0]) cylinder($fn=6, h=10, d=9.6);
            translate([+14, 0, 0]) cylinder($fn=6, h=10, d=9.6);
        }
    }
    
    // nutholder
    difference() {
        intersection() {
            union() {
                cube([9, 12, 19.5]);
                cube([12, 9, 19.5]);
                translate([9, 9, 0]) cylinder($fn=32, r=3, h=19.5);
                
                translate([0, sizeBot[1]-12]) {
                    translate([0, 0, 0]) cube([9, 9, 19]);
                    translate([0, 3, 0]) cube([12, 9, 19]);
                    translate([9, 3, 0]) cylinder($fn=32, r=3, h=19);
                }
            }
            hull() {
                block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=bottomReduction);
                translate([0, 0, bottomHeight]) block(sizeBot[0], sizeBot[1], sizeBot[2]-bottomHeight, crad=crad);
            }
        }
        
        translate([7, 7, 10]) cylinder($fn=32, d=3.3, h=30);
        translate([7, 7, 20-2]) cylinder($fn=32, d=6, h=30);
        translate([7, sizeBot[1]-7, 10]) cylinder($fn=32, d=3.3, h=30);
        translate([7, sizeBot[1]-7, 20-2]) cylinder($fn=32, d=6, h=30);
    }
    
    // hinges
    translate([20+18.2, sizeBot[1]+5.5, 20]) rotate([0, 90, 180]) hinge_bottom();
    translate([-20+sizeBot[0]-2, sizeBot[1]+5.5, 20]) rotate([0, 90, 180]) hinge_bottom();
    
    // nuts
    % translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 12]) rotate([90, 0, 0]) { 
        translate([-14, 0, 0]) M5nut();
        translate([+14, 0, 0]) M5nut();
    }
    
    translate([30, 5, 12]) rotate([90, 0, 0]) M3nut();
    translate([sizeBot[0]-30, 5, 12]) rotate([90, 0, 0]) M3nut();
    
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

module M3nut() {
    color("grey") difference() {
        cylinder($fn=6, h=2, d=6.5);
        translate([0, 0, -1]) cylinder($fn=32, d=3, h=6);
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

module motoE() {
    //cube([154, 75, 10]); // Moto Z
    block(130, 67, 12.3, crad=10);
    translate([65, -0.5, 7]) cube([17, 2, 1.5]);
    translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
    translate([130-23, 67/2, -1]) cylinder($fn=32, h=3, d=13);
}

module nexus5() {
    translate([-5, 68, -26]) rotate([0, 0, -90]) import("nexus5.stl");
}

module nexus5cavity(height) {

    color("red") {
    translate([124, 60.5, 0]) cylinder($fn=32, h=height, d=14);
    translate([124, 5,  0]) cylinder($fn=32, h=height, d=14);
    }
    
    color("green") intersection() {
        translate([120, 5, 0]) cube([20, 55.5, height+2]);
        translate([124.7, 55.5/2+5,  0]) scale([0.18, 1]) cylinder($fn=32, h=height, d=90);
    }
    
    translate([-1, -2, 0]) cube([125, 55.5+14, height]);
    
    translate([102, -3.9, 0])cube([16, 2, height]);
    translate([82, 67, 0])cube([26, 2, height]);

}

module block(width, depth, height, crad=3, red=0) {
//    hull() {    
//        translate([crad+red, crad+red]) cylinder($fn=32, h=height, r=crad);
//        translate([width-crad-red, crad+red]) cylinder($fn=32, h=height, r=crad);
//        translate([crad+red, depth-crad-red]) cylinder($fn=32, h=height, r=crad);
//        translate([width-crad-red, depth-crad-red]) cylinder($fn=32, h=height, r=crad);
//    }
    
    hull() {    
        translate([crad, crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([width-crad, crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([crad, depth-crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([width-crad, depth-crad]) cylinder($fn=32, h=height, r=crad-red);
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