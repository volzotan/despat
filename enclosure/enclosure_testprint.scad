include <hinge.scad>

sizeBot    = [60, 40, 20];
sizeTop    = [60, 40, 24];
lensHole   = [sizeBot[0]-25, 25]; // Nexus 5
//lensHole   = [sizeBot[0]-30, sizeBot[1]/2]; // Moto E

crad       = 10;
w          = 3.2+.1;
wb         = 1.2;

//difference(){
//    union() {
//        bottom();
//        translate([0, sizeTop[1], 44+0.1]) rotate([180, 0, 0]) top();
////        translate([0, 0, 20.1]) seal();
//    }
//    translate([-1, -1, -1]) cube([15, 100, 100]);
//}

//translate([50, 220+80, 0]) rotate([0, -90, 180]) scale([0.5, 0.5, 0.5]) {
//    translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket();
//    bottom();
//    translate([0, sizeTop[1], 45-.7]) rotate([180, 0, 0]) top();
//}
//
//translate([70, 4.7, 10+7]) color("orange") wedge();
////translate([70, -20, 12.8]) rotate([180, 0, 0]) color("orange") wedge();
//
//% translate([0, sizeBot[1]-2.3-0.025, 18.-0.2]) rotate([0, 90, 0]) color([1, 1, 1], 1) cylinder($fn=32, d=1.75, h=100);
//
//
//translate([0, 100, 0]) top();

//translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket_flat();

//% translate([30, 10, 5+0]) nexus5();

//% translate([127, 40, -10]) uvfilter();
//% translate([5, 36, 4]) usbplug();

//% translate([0, sizeBot[1]-2.6, 18.2]) rotate([0, 90, 0]) color([1, 1, 1], 0.6) cylinder($fn=32, d=1.75, h=100);

// --------------------------- PRINT ---------------------------

top();
translate([0, 41])bottom();
//translate([sizeBot[0], 0, 1.3]) rotate([0, 180, 0]) seal();

// ----------------------- add. elements -----------------------

//socket_normal();
//translate([0, 0, 10]) rotate([0, 90, 180]) hinge_bottom(screwed=true);
//translate([sizeBot[0]/2-(44/2), 0, 3.5]) rotate([90, 0, 0]) socket_normal();
//
//translate([32, 09, 10]) color("green") motoE();
//translate([32, 09, 40]) motoEcavity(20);

//translate([30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
//latch();
//translate([0, 16, 0]) latch_knob();
//}

// ------------------------------------------------------------

module top() {
    a = 1;
    c = 2;
    d = 3;
    b = sizeTop[2] - a - c - d - wb + .2;
    
    x = 1;
    y = 4.8;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block(sizeTop[0], sizeTop[1], sizeTop[2]-a, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=x+w);
                    translate([0, 0, a]) 
                        block(sizeTop[0], sizeTop[1], b, crad=crad, red=w);
                }
                
                translate([0, 0, wb+a+b-0.1]) hull() {
                    block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=w);
                    translate([0, 0, c]) 
                        block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=y);
                }
                
                translate([0, 0, wb+a+b+c-0.1]) hull() {
                    block(sizeTop[0], sizeTop[1], d, crad=crad, red=y);
                }
            }
            
            // seal
            translate([0, 0, sizeTop[2]-.1]) color("red") difference() {
                dist = 2;
                width = 1.2;
                height = 1.2-0.3;
                block(sizeTop[0], sizeTop[1], height, crad=crad, red=dist);
                translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height+2, crad=crad, red=dist+width);
            }
            // seal2
            translate([0, 0, sizeTop[2]-.1]) color("red") difference() {
                dist = 2;
                width = 1.2;
                red = 0.2;
                height = 1.2;
                block(sizeTop[0], sizeTop[1], height, crad=crad, red=dist+red);
                translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height+2, crad=crad, red=dist+width-red);
            }
            // seal3
            * translate([0, 0, sizeTop[2]-.1]) {
                color("purple") intersection() {
                    difference() {
                        height = 1.8+0.3;
                        block(sizeTop[0], sizeTop[1], height, crad=crad, red=1.6+0.2);
                        translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height+2, crad=crad, red=1.6+1.4-0.2);
                    }
                    union() {
                        translate([0, 0, 0]) cube([8, 8, 10]);
                        translate([sizeTop[0]-8, 0, 0]) cube([8, 8, 10]);
                        translate([0, sizeTop[1]-8, 0]) cube([8, 8, 10]);
                        translate([sizeTop[0]-8, sizeTop[1]-8, 0]) cube([8, 8, 10]);
                    }
                }
            }
            
        }
        
        // clamp holes
        translate([30, sizeBot[1]+5, 17.6]) rotate([90, 0, 0]) cylinder($fn=32, h=20, d=3.3);
        translate([sizeBot[0]-30, sizeBot[1]+5, 17.6]) rotate([90, 0, 0]) cylinder($fn=32, h=20, d=3.3);
        translate([30, sizeBot[1]-2, 17.6]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        translate([sizeBot[0]-30, sizeBot[1]-2, 17.6]) rotate([90, 0, 0]) cylinder($fn=6, h=20, d=6.6);
    }
    
    // hinges
    move = 0.1;
    translate([+20+18.2, -5.5, 24+move]) rotate([0, -90, 0]) hinge_top();
    translate([-20+sizeTop[0]-2, -5.5, 24+move]) rotate([0, -90, 0]) hinge_top();
    
    % translate([62, 1, wb+0.3]) anker();
}
                    

module bottom() {   
    
    a = 2;
    c = 2;
    d = 4;
    b = sizeBot[2] - a - c - d - wb + .2;
    
    x = 2;
    y = 4.8;
    
    difference() {
        union() {
            difference() {
                
                hull() { // outer rim reduction
                    red = 1;
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=red);
                    translate([0, 0, red]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-red, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=x+w);
                    translate([0, 0, a]) 
                        block(sizeBot[0], sizeBot[1], b, crad=crad, red=w);
                }
                
                translate([0, 0, wb+a+b-0.1]) hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=w);
                    translate([0, 0, c]) 
                        block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=y);
                }
                
                translate([0, 0, wb+a+b+c-0.1]) hull() {
                    block(sizeBot[0], sizeBot[1], d, crad=crad, red=y);
                }
            }
            
            // camera lens reinforcement
            * translate([lensHole[0], lensHole[1], wb]) hull() {
                cylinder($fn=64, h=0.1, d=37+5);
                translate([0, 0, 1.2]) cylinder($fn=64, h=0.1, d=37+3);
            }
            
            
            // hinge nut support
            translate([10, sizeBot[1]-4.8, 1.2]) cube([40, 4.8, 16]);
        }
        
       
        // clamp holes
        translate([30, 5, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
        translate([30, 10+2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        
        // hinge holes
        hinge_hole_height = 11.5;
        translate([17.2, sizeBot[1]+1, 11.5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=10);
        translate([17.2, sizeBot[1]-3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=8);
        translate([43.2, sizeBot[1]+1, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=10);
        translate([43.2, sizeBot[1]-3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=8);
        
        // seal
        translate([0, 0, sizeBot[2]-2.2]) color("red") difference() {
            dist = 1.6 + 0.05; // distance outer wall / 1.65 + 1.9 + 1.25 = 4.8
            height = 3;
            width = 1.9;
            union() {
                hull() {
                    block(sizeBot[0], sizeBot[1], height-.3, crad=crad, red=dist);
                    translate([0, 0, -.5]) block(sizeBot[0], sizeBot[1], height, crad=crad, red=dist+0.5); // lower triange #1
                }
            }
            
            translate([0, 0, -.1]) block(sizeBot[0], sizeBot[1], height+2, crad=crad, red=dist+width);
            translate([0, 0, -.6]) color("orange") hull() {
                block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=dist+width-0.5);                          // lower triange #1
                translate([0, 0, 0.5])block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=dist+width);
            }
        }
        // seal 2
        translate([0, 0, sizeBot[2]-0.3]) color("orange") difference() {
            dist = 1.65; // distance outer wall
            height = 3;
            width = 1.85;
            
            block(sizeBot[0], sizeBot[1], 1, crad=crad, red=dist-0.3);
            block(sizeBot[0], sizeBot[1], 1, crad=crad, red=dist+width+0.3);
        }
        
        
        // stress relief / anti warping holes
//        p_reliefholes = [[0, 0], [3, 3], [12, 3], [15, 5], [15, 0]];
//        translate([43, 15-1, -1]) rotate([90, 0, -90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([60, 15-1, -1]) rotate([90, 0, -90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([113, 15-1, -1]) rotate([90, 0, -90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([sizeBot[0]-14, 50, -1]) rotate([90, 0, 0]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([sizeBot[0]-14, 70, -1]) rotate([90, 0, 0]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([43-3, sizeBot[1]-14, -1]) rotate([90, 0, 90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([60-3, sizeBot[1]-14, -1]) rotate([90, 0, 90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([113-3, sizeBot[1]-14, -1]) rotate([90, 0, 90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([133-3, sizeBot[1]-14, -1]) rotate([90, 0, 90]) linear_extrude(height=3) polygon(p_reliefholes);
//        translate([153-3, sizeBot[1]-14, -1]) rotate([90, 0, 90]) linear_extrude(height=3) polygon(p_reliefholes);
    }
    
    // hinges
    % translate([20+18.2, sizeBot[1]+5.5, 20]) rotate([0, 90, 180]) color("purple") hinge_bottom(screwed=true);

    // hinge support
//    translate([17-.2, sizeBot[1], 2])                                   hinge_support();
//    translate([sizeBot[0]-30-13.4, sizeBot[1], 2])                      hinge_support();
//    translate([30+13.4, sizeBot[1], 14+2]) rotate([0, 180, 0])          hinge_support();
//    translate([sizeBot[0]-17+.2, sizeBot[1], 14+2]) rotate([0, 180, 0]) hinge_support();
    
    // nuts
    % translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 11.5]) rotate([90, 0, 0]) { 
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

module usbplug() {
    color("grey") {
        cube([17, 12, 8]);
    }
}

module M5nut() {
    % color("grey") difference() {
        cylinder($fn=6, h=4, d=9.5);
        translate([0, 0, -1]) cylinder($fn=32, d=5, h=6);
    }
}

module M3nut() {
    % color("grey") difference() {
        cylinder($fn=6, h=2, d=6.5);
        translate([0, 0, -1]) cylinder($fn=32, d=3, h=6);
    }
}

module DIN912screw(length) {
    % difference() {
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
    color("grey") {
        translate([65, -0.5, 7]) cube([17, 2, 1.5]);
        translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
        translate([130-22, 67/2, -1]) cylinder($fn=32, h=3, d=13);
    }
}

module nexus5() {
    translate([-5, 68, -26]) rotate([0, 0, -90]) import("nexus5.stl");
}

module motoEcavity(height) {
    difference() {
        translate([-10, 0]) block(143, 67, height, crad=5);
//        points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
//        translate([-12, 68-.1, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
    }
    
    
    translate([64, -4.9, 0])cube([42, 5, height]);
//    translate([82, 65, 0])cube([26, 5, height]);
    
    // wedge
    translate([40, 61+5.5+3, 0]) cube([40, 2, height]);
    translate([40+2, 61+5.5, 0]) cube([36, 5, height]);
}

module nexus5cavity(height) {
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
    
    
    translate([102, -4.9, 0])cube([16, 5, height]);
    translate([82, 65, 0])cube([26, 5, height]);
    
    // wedge
    translate([40, -5.5, 0]) cube([40, 2, height]);
    translate([40+2, -5.5, 0]) cube([36, 5, height]);
}

module anker() {
    translate([0, 11, 11]) rotate([0, 90, 0]) hull() {
        translate([0, 11, 11]) cylinder($fn=32, d=22, h=91);
        translate([0, 62.6-11, 11]) cylinder($fn=32, d=22, h=91);
    }
}

module anker_cutout() {
    tol = 0.3;
    dist = 9;
    
    translate([0, 11, 11]) rotate([0, 90, 0]) hull() {
        translate([0, 11, 11]) cylinder($fn=32, d=22+tol, h=100);
        translate([0, 62.6-11, 11]) cylinder($fn=32, d=22+tol, h=100);
        
        translate([-30, 11, 11]) cylinder($fn=32, d=22+tol, h=100);
        translate([-30, 62.6-11-dist, 11]) cylinder($fn=32, d=22+tol, h=100);
    }
}

module hinge_support() {
    points_hinges = [[0, 0], [3, 0], [3, 1], [1, 1]];
    points_hinges2 = [[0, 0], [14, 0], [12, 1], [2, 1]];
    
    intersection() {
        linear_extrude(height=14) polygon(points_hinges);
        translate([0, 0, 14]) rotate([0, 90, 0]) linear_extrude(height=5) polygon(points_hinges2);
    }
}

module block(width, depth, height, crad=3, red=0) {
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