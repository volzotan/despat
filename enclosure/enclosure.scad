 include <hinge.scad>

// --- standard
sizeBot    = [170, 86, 20];
sizeTop    = [170, 86, 24];
sizeTopF   = [170, 86, 10];

// --- oversized
//sizeBot    = [190, 86+7, 20];
//sizeTop    = [190, 86+7, 24];
//sizeTopF   = [190, 86+7, 10];

// --- oversized - short
sizeBot    = [167, 86+7, 20];
sizeTop    = [167, 86+7, 24];
sizeTopF   = [167, 86+7, 10];

// --- standard - short
sizeBot    = [150, 86, 20];
sizeTop    = [150, 86, 24];
sizeTopF   = [150, 86, 10];

crad       = 6;
w          = 2.4+.1; //3.2+.1;
wb         = 1.2;

seal_thickness = 1; // private

seal_hard       = false; 
seal_rubber     = true;
seal_flex       = false;

cavity_nexus5   = false;
cavity_motoE    = true;
cavity_ZTEAxon  = false;

//lensHole   = [sizeBot[0]-25, 25]; // Nexus 5
lensHole   = [sizeBot[0]-30, sizeBot[1]/2]; // Moto E
//lensHole   = [sizeBot[0]-29.5, 47.5]; // ZTE Axon 7


//translate([11, 11, 8]) motoE();
//translate([07, 9.5, 8]) color("blue") zteaxon7();

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

//% translate([0, sizeBot[1]-2.6, 17.9]) rotate([0, 90, 0]) color([1, 1, 1], 0.6) cylinder($fn=32, d=1.75, h=100);


// --------------------------- TEST ----------------------------

//bottom();
//translate([0, sizeTop[1], sizeBot[2] + sizeTop[2] + 0.2]) rotate([180, 0, 0]) top();
//translate([0, 0, sizeBot[2]-0.5+0.1]) color("grey") seal();
//
//translate([30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
//    latch();
//    translate([0, 14.5, 0]) latch_knob();
//}
//
//translate([sizeBot[0]-30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
//    latch();
//    translate([0, 14.5, 0]) latch_knob();
//}
//
//translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket_normal();

// --------------------------- TEST2 ---------------------------

bottom();
//translate([0, sizeTop[1]+20, 0]) top();
translate([0, sizeTop[1]+20, 0]) top_flat();
translate([0, 0, 30]) color("grey") seal();

//translate([30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
//    latch();
//    translate([0, 14.5, 0]) latch_knob();
//}
//
//translate([sizeBot[0]-30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
//    latch();
//    translate([0, 14.5, 0]) latch_knob();
//}

translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket_normal();

// --------------------------- TEST3 ---------------------------

//bottom();
//translate([sizeTop[0], 0, sizeBot[2] + sizeTopF[2] + 0.2]) rotate([180, 0, 180]) top_flat();
//translate([0, 0, sizeBot[2]-0.5+0.1]) color("grey") seal();
//
//translate([20, sizeBot[1]+0.1, 12+13.2]) rotate([90, 180, 180]) color("purple") {
//    latch(distance=-1.3);
//    translate([0, 14.5-1.3, 0]) latch_knob();
//}
//
//translate([sizeBot[0]-20, sizeBot[1]+0.1, 12+13.2]) rotate([90, 180, 180]) color("purple") {
//    latch(distance=-1.3);
//    translate([0, 14.5-1.3, 0]) latch_knob();
//}
//
//translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket_normal();

// --------------------------- PRINT ---------------------------

//top();
//top_flat();
//bottom();
//translate([sizeBot[0], 0, 1.3]) rotate([0, 180, 0]) seal();
//
//translate([0, 0]) {
//    latch(); translate([0, 14.5, 0]) latch_knob();
//}
//translate([13, 0]){
//    latch(); translate([0, 14.5, 0]) latch_knob();
//}
//translate([20, 10]) rotate([90, 0, 0]) socket_normal(); 
//
//translate([20+18.2, sizeBot[1]+5.5, 0]) rotate([90, 0, 0]) hinge_bottom(screwed=true);

// ----------------------- add. elements -----------------------

//socket_normal();
//translate([0, 0, 10]) rotate([0, 90, 180]) hinge_bottom(screwed=true);
//translate([sizeBot[0]/2-(44/2), 0, 3.5]) rotate([90, 0, 0]) socket_normal();
//
//translate([32, 09, 10]) color("green") motoE();
//translate([32, 09, 40]) motoEcavity(20);

//translate([30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
//    latch();
//    translate([0, 14.5, 0]) latch_knob();
//}

// ------------------------------------------------------------

module latch_knob() {
    height = 4.5;
    
    difference() {
        union() {
            translate([]) cylinder($fn=32, d=8, h=height);
        }
        
        translate([0, 0, -1]) cylinder($fn=32, d=3.3, h=height+2);
        translate([0, 0, height-3.3]) cylinder($fn=32, d=6, h=height+2);
    }
}

module latch(distance=0) {
    height = 4.5;
    
    depth = 18+distance;
    depth_knob = 14.5+distance;
    
//    cylinder($fn=32, d=37, h=10);
    
    difference() {
        union() {
//            points = [[-3, 0], [3, 0], [10, 30], [-10, 30], [-10, 25], [5, 25]];
//            translate([]) linear_extrude(height=height) polygon(points);
            
            hull() {
                translate([]) cylinder($fn=32, d=10, h=height);
                translate([4, 8]) cylinder($fn=32, d=4, h=height);
                translate([4-2+2, depth+2, 0]) cylinder($fn=32, d=4, h=height);
                translate([-4-0.5, depth+2.5, 0]) cylinder($fn=32, d=3, h=height);
            }
            
            hull() {
                x = 4;
                y = 9.5+distance;
                translate([x, y]) cylinder($fn=32, d=2, h=height+2);
                translate([x+1, y]) cylinder($fn=32, d=2, h=height+2);
                translate([x, y+10]) cylinder($fn=32, d=2, h=height+2);
                translate([x+1, y+10]) cylinder($fn=32, d=2, h=height+2);
            }
        }
        
        translate([0, 0, -1]) hull() {
            translate([-20, depth_knob-3]) cylinder($fn=32, d=9, h=height+2);
            translate([-20, depth_knob-5]) cylinder($fn=32, d=9, h=height+2);
            translate([0, depth_knob]) cylinder($fn=32, d=9, h=height+2);
        }
        
        translate([0, 0, -1]) cylinder($fn=32, d=3.3, h=height+2);
        translate([0, 0, height-3.3]) cylinder($fn=32, d=8, h=height+2);
    }
}

module wedge() {
    tol = 0.3;
    height = 12.5;
    
    translate([tol, 0.1+tol, tol]) cube([40-tol*2, 1.7-tol*2, height]);
    translate([2+tol, 2-tol*2-0.2, tol]) cube([36-tol*2, 2-tol*2+0.1, height]);
    
    points = [[0, 0], [1, 0], [1, height], [-1.3, height]];
    translate([38-tol, 2.9, tol]) rotate([90, 0, -90]) linear_extrude(height=36-tol*2) polygon(points); 
}

module socket_normal() {
    
    // socket_normal works either with DIN 7984 M5 or standard DIN 912 M5 screws
    // DIN 934 1/4 inch or the slightly higher different imperial nut types are okay
    
    width = 44;
    depth = 16;
    height = 7.5;
    
    difference() {
        block(width, depth, height, crad=2);
        
        translate([(width)/2-14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2+14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2-14, depth/2, 1.8]) cylinder($fn=32, h=20, d=9);
        translate([(width)/2+14, depth/2, 1.8]) cylinder($fn=32, h=20, d=9);
        
        translate([(width)/2, depth/2, -10]) cylinder($fn=32, h=20, d=7);
        translate([(width)/2, depth/2, -1]) cylinder($fn=6, h=1+6, d=13.15);  // +1 safety margin for long fastening screws
    }
}

module socket_flat() {
    
    // socket_flat requires DIN 7984 M5 screws and DIN 934 1/4 inch nuts
    
    width = 44;
    depth = 16;
    height = 6;
    
    difference() {
        block(width, depth, height, crad=2);
        
        translate([(width)/2-14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2+14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2-14, depth/2, height-3.5-0.3]) cylinder($fn=32, h=20, d=9);
        translate([(width)/2+14, depth/2, height-3.5-0.3]) cylinder($fn=32, h=20, d=9);
        
        translate([(width)/2, depth/2, -10]) cylinder($fn=32, h=20, d=7);
        translate([(width)/2, depth/2, -1]) cylinder($fn=6, h=1+5, d=13.15);  // +1 safety margin for long fastening screws
    }
}

module seal() {
    height = 1;
    
    difference() {
        block2(sizeBot[0], sizeBot[1], height, crad=crad);    
        translate([0, 0, -1]) block2(sizeBot[0], sizeBot[1], height+2, crad=crad, red=4.5);  
    }     
}



module top_flat() {
    
    a = 2;
    c = 2;
    d = 3;
    b = sizeTopF[2] - a - c - d - wb + .2;
    
    x = 2;
    y = 4.8;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block2(sizeTopF[0], sizeTopF[1], sizeTopF[2]-a-seal_thickness/2, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=x+w);
                    translate([0, 0, a]) 
                        block2(sizeTopF[0], sizeTopF[1], b, crad=crad, red=w);
                }
                
                translate([0, 0, wb+a+b-0.1]) hull() {
                    block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=w);
                    translate([0, 0, c]) 
                        block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=y);
                }
                
                translate([0, 0, wb+a+b+c-0.1]) hull() {
                    block2(sizeTopF[0], sizeTopF[1], d, crad=crad, red=y);
                }
            }
            
            // hinge screw reinforcement
            intersection() {
                union() { 
                    // hinge screw reinforcement
                    points_r = [[0, 0], [20, 0], [15, 4.8], [5, 4.8]];
                    translate([30, sizeTopF[1], 0]) rotate([0, 0, 180]) linear_extrude(height=sizeTopF[2]-seal_thickness/2) polygon(points_r);
                    translate([sizeTopF[0]+20-30, sizeTopF[1], 0]) rotate([0, 0, 180]) linear_extrude(height=sizeTopF[2]-seal_thickness/2) polygon(points_r);
                }
                
                // outer hull
                hull() {
                    block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block2(sizeTopF[0], sizeTopF[1], sizeTopF[2]-a-seal_thickness/2, crad=crad);
                }
            }
        }
        
        // rotation clamp holes
        translate([20, sizeTopF[1]+5, 5])               rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
        translate([sizeTopF[0]-20, sizeTopF[1]+5, 5])   rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
        translate([20, sizeTopF[1]-2, 5])               rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
        translate([sizeTopF[0]-20, sizeTopF[1]-2, 5])   rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
    }
    
    // hinges
    intersection() {
        union() {
            move = 0.1;
            translate([31, -5.5, sizeTopF[2]+move])             rotate([0, -90, 0]) hinge_top();
            translate([sizeTopF[0]-15, -5.5, sizeTopF[2]+move]) rotate([0, -90, 0]) hinge_top();
            translate([16, 0]) cube([13.6, 2, 2]);
            translate([sizeTopF[0]-30, 0]) cube([13.6, 2, 2]);
        }
        translate([-100, -100]) cube([500, 500, 100]);
    }
    
    translate([18-5, sizeTopF[1]-2, 0]) hull() {
        translate([2, 0, 0]) cube([14-4, 2, 1]);
        translate([0, 0, 2]) cube([14, 2, 1]);
    }    
    translate([sizeTopF[0]-14-18+5, sizeTopF[1]-2, 0]) hull() {
        translate([2, 0, 0]) cube([14-4, 2, 1]);
        translate([0, 0, 2]) cube([14, 2, 1]);
    }
}

module top() {
    
    a = 1;
    c = 2;
    d = 3;
    b = sizeTop[2] - a - c - d - wb + .2;
    
    x = 2;
    y = 4.8;
    
    difference() {
        union() {
            difference() {
                hull() {
                    block2(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block2(sizeTop[0], sizeTop[1], sizeTop[2]-a-seal_thickness/2, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block2(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=x+w);
                    translate([0, 0, a]) 
                        block2(sizeTop[0], sizeTop[1], b, crad=crad, red=w);
                }
                
                translate([0, 0, wb+a+b-0.1]) hull() {
                    block2(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=w);
                    translate([0, 0, c]) 
                        block2(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=y);
                }
                
                translate([0, 0, wb+a+b+c-0.1]) hull() {
                    block2(sizeTop[0], sizeTop[1], d, crad=crad, red=y);
                }
            }
            
//            // seal
//            translate([0, 0, sizeTop[2]-.1]) color("red") difference() {
//                dist = 2;
//                width = 1.2;
//                height = 1.5-0.3;
//                block2(sizeTop[0], sizeTop[1], height, crad=crad, red=dist);
//                translate([0, 0, -1]) block2(sizeTop[0], sizeTop[1], height+2, crad=crad, red=dist+width);
//            }
//            // seal2
//            translate([0, 0, sizeTop[2]-.1]) color("red") difference() {
//                dist = 2;
//                width = 1.2;
//                red = 0.2;
//                height = 1.5;
//                block2(sizeTop[0], sizeTop[1], height, crad=crad, red=dist+red);
//                translate([0, 0, -1]) block2(sizeTop[0], sizeTop[1], height+2, crad=crad, red=dist+width-red);
//            }
//            // seal3
//            translate([0, 0, sizeTop[2]-.1]) {
//                dist = 2;
//                width = 1.2;
//                red = 0.2;
//                height = 1.5+0.3;
//                color("purple") intersection() {
//                    difference() {
//                        block2(sizeTop[0], sizeTop[1], height, crad=crad, red=dist+red);
//                        translate([0, 0, -1]) block2(sizeTop[0], sizeTop[1], height+2, crad=crad, red=dist+width-red);
//                    }
//                    union() {
//                        translate([0, 0, 0]) cube([8, 8, 10]);
//                        translate([sizeTop[0]-8, 0, 0]) cube([8, 8, 10]);
//                        translate([0, sizeTop[1]-8, 0]) cube([8, 8, 10]);
//                        translate([sizeTop[0]-8, sizeTop[1]-8, 0]) cube([8, 8, 10]);
//                    }
//                }
//            }
            
            //inlay
            intersection() {
                union() { 
                    translate([sizeTop[0]-140, 0]) union() {
                        difference() {
                            translate([10, 0, 0]) {
                                cube([200, sizeTop[1], sizeTop[2]-seal_thickness/2]);
                                                                                            
                                % translate([20.5, 1, wb+0.3]) anker();
                            } 
                   
                            points = [[0, 0], [sizeTop[1], 0], [sizeTop[1], 20], [sizeTop[1]-10, 30], [10, 30], [0, 20]];
                            translate([10-1, sizeTop[1]]) rotate([0, 0, -90]) linear_extrude(height=30) polygon(points);
                        
                            translate([21.75, 5, wb]) color("purple") anker_cutout();
                            
                            // button
                            translate([41+09, 6, 5]) block(14, 20, 50);
                            
                            // wedge
                            translate([80, 7, 1]) cube([40, 3, sizeTop[2]]);
                            translate([80+5, 7+2, 1]) cube([30, 20, sizeTop[2]]);
                        }
                        translate([39, 0]) cube([2, sizeTop[1], 5]);
                    }
                    // hinge screw reinforcement
                    points_r = [[0, 0], [20, 0], [15, 4.8], [5, 4.8]];
                    translate([40, sizeTop[1], 0]) rotate([0, 0, 180]) linear_extrude(height=sizeTop[2]-seal_thickness/2) polygon(points_r);
                }
                
                // outer hull
                hull() {
                    block2(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block2(sizeTop[0], sizeTop[1], sizeTop[2]-a-seal_thickness/2, crad=crad);
                }
            }
        }
        
        // clamp holes
        translate([30, sizeTop[1]+5, 17.6])             rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
        translate([sizeTop[0]-30, sizeTop[1]+5, 17.6])  rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
        translate([30, sizeTop[1]-2, 17.6])             rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
        translate([sizeTop[0]-30, sizeTop[1]-2, 17.6])  rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
    }
    
    // hinges
    move = 0.1;
    translate([+20+18.2, -5.5, 24+move])            rotate([0, -90, 0]) hinge_top();
    translate([-20+sizeTop[0]-2, -5.5, 24+move])    rotate([0, -90, 0]) hinge_top();
}
                    

module bottom() { 
    
    a = 1;
    c = 2;
    d = 3;
    b = sizeBot[2] - a - c - d - wb + .2;
    
    x = 2;
    y = 4.8;
    
    difference() {
        union() {
            difference() {
                
                hull() { // outer rim reduction
                    red = 1;
                    block2(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=red);
                    translate([0, 0, red]) 
                        block2(sizeBot[0], sizeBot[1], sizeBot[2]-red-seal_thickness/2, crad=crad);
                }
                
                translate([0, 0, wb]) hull() {
                    block2(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=x+w);
                    translate([0, 0, a]) 
                        block2(sizeBot[0], sizeBot[1], b, crad=crad, red=w);
                }
                
                translate([0, 0, wb+a+b-0.1]) hull() {
                    block2(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=w);
                    translate([0, 0, c]) 
                        block2(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=y);
                }
                
                translate([0, 0, wb+a+b+c-0.1]) hull() {
                    block2(sizeBot[0], sizeBot[1], d, crad=crad, red=y);
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
                    translate([20, w, 0]) cube([sizeBot[0]-20, sizeBot[1]-2*w, 20]); //20-1]);
                    
                    // CAVITY
                    if (cavity_nexus5)  translate([30, 10, 7]) color("red") nexus5_cavity(20); 
                    if (cavity_motoE)   translate([sizeBot[0]-141, 10, 7]) color("red") motoE_cavity(20); 
                    if (cavity_ZTEAxon) translate([sizeBot[0]-160.25, 10, 7]) color("red") zteaxon7_cavity(20); 
                    
                    points = [[0, 0], [sizeBot[1], 0], [sizeBot[1], 20], [sizeBot[1]-20, 30], [20, 30], [0, 20]];
                    translate([0, sizeBot[1]]) rotate([0, 0, -90]) linear_extrude(height=30) polygon(points);
                    
                    * hull() {
                        translate([30, 9, 15-.1]) color("red") nexus5cavity(0.1);
                        translate([30-1, w, 20-1]) cube([sizeBot[0]-19, sizeBot[1]-2*w, 1]);
                    }
                }
                
                // outer hull
                hull() {
                    red = 1;
                    block2(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=red);
                    translate([0, 0, red]) 
                        block2(sizeBot[0], sizeBot[1], sizeBot[2]-red-seal_thickness/2, crad=crad);
                }
            }
            
            // hinge nut support
            intersection() {
                union() {
                    // hinge screw reinforcement
                    points_r = [[0, 0], [20, 0], [15, 4.8], [5, 4.8]];
                    translate([30, sizeBot[1], 1.2]) rotate([0, 0, 180]) linear_extrude(height=sizeBot[2]-seal_thickness/2) polygon(points_r);
                    translate([0, 0, 0]) rotate([0, 0, 1.2]) linear_extrude(height=sizeBot[2]-seal_thickness/2) polygon(points_r); 
                }
            
                block2(sizeBot[0], sizeBot[1], sizeBot[2], crad=crad);
            }
        }
        
        // camera lens
        translate([lensHole[0], lensHole[1], -1]) {
            down = 39.4+0.7;
            up   = 36.9+0.5;
            cylinder($fn=64, h=1+3.60+0.3, d=down);
            translate([0, 0, 1+3.60+0.3-0.1]) cylinder($fn=64, h=2, d1=down, d=up);
            translate([0, 0, 6-.3]) cylinder($fn=64, h=2, d=up);
            translate([0, 0, 6+2-.3-.1]) cylinder($fn=64, h=3, d1=up, d2=34);
        }
        
        // socket screw holes
        translate([sizeBot[0]/2, sizeBot[1]+5, 11.5]) rotate([90, 0, 0]) {
            translate([-14, 0, 0]) cylinder($fn=32, h=50, d=5.3);
            translate([+14, 0, 0]) cylinder($fn=32, h=50, d=5.3);
        }
        translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 11.5]) rotate([90, 0, 0]) { 
            translate([-14, 0, 0]) cylinder($fn=6, h=50, d=9.6);
            translate([+14, 0, 0]) cylinder($fn=6, h=50, d=9.6);
        }
        
        // rotation clamp holes
        translate([20, sizeBot[1]+10, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
        translate([sizeBot[0]-20, sizeBot[1]+10, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
        translate([20, sizeBot[1]-2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
        translate([sizeBot[0]-20, sizeBot[1]-2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
        
        // hinge holes
        hinge_hole_height = 11.5;
        translate([10, 20, 11.5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
        translate([10, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
        translate([10+26, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
        translate([10+26, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
        
        translate([sizeBot[0]-10, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
        translate([sizeBot[0]-10, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
        translate([sizeBot[0]-10-26, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
        translate([sizeBot[0]-10-26, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
        
//        // seal
//        translate([0, 0, sizeBot[2]-2.5]) color("red") difference() {
//            dist = 1.6 + 0.05; // distance outer wall / 1.65 + 1.9 + 1.25 = 4.8
//            height = 3;
//            width = 1.9;
//            union() {
//                hull() {
//                    block(sizeBot[0], sizeBot[1], height-.3, crad=crad, red=dist);
//                    translate([0, 0, -.5]) block(sizeBot[0], sizeBot[1], height, crad=crad, red=dist+0.5); // lower triange #1
//                }
//            }
//            
//            translate([0, 0, -.1]) block(sizeBot[0], sizeBot[1], height+2, crad=crad, red=dist+width);
//            translate([0, 0, -.6]) color("orange") hull() {
//                block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=dist+width-0.5);                          // lower triange #1
//                translate([0, 0, 0.5])block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=dist+width);
//            }
//        }
//        // seal 2
//        translate([0, 0, sizeBot[2]-0.3]) color("orange") difference() {
//            dist = 1.65; // distance outer wall
//            height = 3;
//            width = 1.85;
//            
//            block(sizeBot[0], sizeBot[1], 1, crad=crad, red=dist-0.3);
//            block(sizeBot[0], sizeBot[1], 1, crad=crad, red=dist+width+0.3);
//        }
        
        
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
    % translate([9+06, -5.5-0.1, 20]) rotate([0, 90, 0]) color("purple") hinge_bottom(screwed=true);
    % translate([sizeBot[0]-25-06, -5.5-0.1, 20]) rotate([0, 90, 0]) color("purple") hinge_bottom(screwed=true);
    
    // nuts
    % translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 11.5]) rotate([90, 0, 0]) { 
        translate([-14, 0, 0]) M5nut();
        translate([+14, 0, 0]) M5nut();
    }
    
    translate([30, 5, 12]) rotate([90, 0, 0]) M3nut();
    translate([sizeBot[0]-30, 5, 12]) rotate([90, 0, 0]) M3nut();
    
}

// ------------------------------------------ MODELS -------------------------------------------------

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

module anker() {
    translate([0, 11, 11]) rotate([0, 90, 0]) hull() {
        translate([0, 11, 11]) cylinder($fn=32, d=22, h=91);
        translate([0, 62.6-11, 11]) cylinder($fn=32, d=22, h=91);
    }
}

// ------------------------------------------- MISC --------------------------------------------------

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

module block(width, depth, height, crad=3, red=0) {
    hull() {    
        translate([crad, crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([width-crad, crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([crad, depth-crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([width-crad, depth-crad]) cylinder($fn=32, h=height, r=crad-red);
    }
}

module block2(width, depth, height, crad=3, red=0) {
    
    // pythagorean theorem
    redp = sqrt(red*red + red*red) - red;
    
    points = [  [0+red, crad+redp], [crad+redp, 0+red],
                [width-crad-redp, 0+red], [width-red, crad+redp],
                [width-red, depth-crad-redp], [width-crad-redp, depth-red],
                [crad+redp, depth-red], [0+red, depth-crad-redp]];
    
    linear_extrude(height=height) polygon(points);
}

// ---------------------------------------- SMARTPHONES ----------------------------------------------

module motoE() {
    //cube([154, 75, 10]); // Moto Z
    block(130, 67, 12.5, crad=10);
    color("grey") {
        translate([65, -0.5, 7]) cube([17, 2, 1.5]);
        translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
        translate([130-22, 67/2, -1]) cylinder($fn=32, h=3, d=13);
    }
}

module nexus5() {
    translate([-5, 68, -26]) rotate([0, 0, -90]) import("nexus5.stl");
}

module motoE_cavity(height) {
    difference() {
        translate([-10, 0]) block(143, 67.75, height, crad=8);
//        points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
//        translate([-12, 68-.1, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
        
        translate([-10, 67.75-1.5, 12.5]) rotate([0, 90, 0]) linear_extrude(height=130+15) polygon([[0, 0], [4, 1.5], [0, 1.5]]);
    }
    
    // buttons
    translate([65, -3.9, 0]) linear_extrude(height=height) polygon([[3, 1], [41-12, 1], [41-11, 0], [41, 0], [41, 4], [0, 4]]);//cube([41, 5, height]);
    
    // wedge
    translate([32, -5, 0]) cube([30.5, 2.3, height]);
    translate([32+2, -5, 0]) cube([26.5, 6, height]);   
    
    translate([84, 37, -0.3]) rotate([0, 0, 180]) linear_extrude(height=10) color("DarkRed" ) text("MOTO E");
}

module nexus5_cavity(height) {
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
    
    translate([20, 28, -0.3]) linear_extrude(height=10) color("DarkRed" ) text("NEXUS 5");
    
    translate([102, -4.9, 0])cube([16, 5, height]);
    translate([82, 65, 0])cube([26, 5, height]);
    
    // wedge
    translate([40, -5.5, 0]) cube([40, 2, height]);
    translate([40+2, -5.5, 0]) cube([36, 5, height]);
}

module zteaxon7() {
    block(151.6, 74.8, 9, crad=12-5);
    color("grey") {
        translate([151.6-31.5-24, -0.5, 4.5]) cube([24, 2, 1.5]);
        translate([151.6-74, -0.5, 4.5]) cube([11, 2, 1.5]);
//        translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
        translate([151.6-21, 74.8/2, -1]) cylinder($fn=32, h=3, d=13);
    }
}

module zteaxon7_cavity(height) {
    
    size = [151.6+1, 74.8+1+2];
    
    difference() {
        union() {
            translate([-0.5, -0.5, 0]) block(size[0], size[1], height, crad=4);
        }
    
        points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
        translate([-1, size[1]-1.7, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
    }
    
    translate([-10, -0.5]) cube([15, size[1]-1-2, height]);
    
    // text
    translate([15, size[1]/2-5, -0.3]) linear_extrude(height=10) color("DarkRed") text("ZTE AXON 7");
    
    // buttons
    translate([75, -4, 0])cube([48, 5, height]);
    
    // wedge
    translate([35, -5, 0]) cube([30.5, 2.3, height]);
    translate([35+2, -5, 0]) cube([26.5, 6, height]);   
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