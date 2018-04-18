 include <hinge.scad>

// --- standard
//sizeBot    = [170, 86, 20];
//sizeTop    = [170, 86, 24];
//sizeTopF   = [170, 86, 10];

// --- oversized
//sizeBot    = [190, 86+7, 20];
//sizeTop    = [190, 86+7, 24];
//sizeTopF   = [190, 86+7, 10];

// --- oversized - short
sizeBot    = [167, 86+7, 20];
sizeTop    = [167, 86+7, 24];
sizeTopF   = [167, 86+7, 10];

// --- standard - short
//sizeBot    = [150, 86, 20];
//sizeTop    = [150, 86, 24];
//sizeTopF   = [150, 86, 10];

crad       = 6;
w          = 2.4+.1; //3.2+.1;
wb         = 1.2;

seal_thickness = 1; // private
heat_inset_diam = 4.1;

screw_inset     = false;
heat_inset      = true;

seal_hard       = false; 
seal_rubber     = true;
seal_flex       = false;

//include<phone_NEXUS5.scad>;
//include<phone_MOTOE2.scad>;
//include<phone_MOTOZ1.scad>;
include<phone_ZTEAXON7.scad>;
//include<phone_GENERIC.scad>;


//translate([11, 11, 8]) motoE();
//% translate([07, 9.9, 8]) color("blue") zteaxon7();
//% translate([06.5, 9.2, 8+0]) motoZ();

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

// --------------------------- TEST0 ---------------------------

//intersection() {
//    union() {
//        bottom();
////        translate([sizeTop[0], 0, sizeBot[2] + sizeTopF[2] + 0.2]) rotate([180, 0, 180]) top_flat();
//        translate([0, 0, sizeBot[2]-0.5+0.1]) color("grey") seal();
//
////        translate([20, sizeBot[1]+0.1, 12+13.2]) rotate([90, 180, 180]) color("purple") {
////            latch(distance=-1.3);
////            translate([0, 14.5-1.3, 0]) latch_knob();
////        }
////
////        translate([sizeBot[0]-20, sizeBot[1]+0.1, 12+13.2]) rotate([90, 180, 180]) color("purple") {
////            latch(distance=-1.3);
////            translate([0, 14.5-1.3, 0]) latch_knob();
////        }
//
//        translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket_normal();
//        
//        translate([95.1, 12.7, 7.2]) color("green") wedge_semiflex_motoZ();
//    }
//    
//    translate([25+0, -10, -1]) cube([sizeBot[0]+20, sizeBot[1]+20, sizeBot[2]+20]);
//}

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

//bottom();
////translate([0, sizeTop[1]+20, 0]) top();
//translate([0, sizeTop[1]+20, 0]) top_flat();
//translate([0, 0, 30]) color("grey") seal();
//
////translate([30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
////    latch();
////    translate([0, 14.5, 0]) latch_knob();
////}
////
////translate([sizeBot[0]-30, 0.1, 12]) rotate([90, 0, 0]) color("purple") {
////    latch();
////    translate([0, 14.5, 0]) latch_knob();
////}
//
////translate([41, 05, 30]) wedge();

// --------------------------- TEST3 ---------------------------

//bottom();
//translate([0, 0, 50]) color("blue") phone();
//translate([0, sizeTopF[1], sizeBot[2] + sizeTopF[2] + 0.2]) rotate([180, 0, 0]) top_flat();
//translate([0, 0, sizeBot[2]-0.5+0.1]) color("grey") seal();
//
//translate([20, -0.1, 12+13.2]) rotate([90, 180, 0]) color("purple") {
//   latch(distance=-1.3);
//   translate([0, 14.5-1.3, 0]) latch_knob();
//}
//
//translate([sizeBot[0]-20, -0.1, 12+13.2]) rotate([90, 180, 0]) color("purple") {
//   latch(distance=-1.3);
//   translate([0, 14.5-1.3, 0]) latch_knob();
//}

//translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket_normal();

// --------------------------- PRINT ---------------------------

//top();
//top_flat();
//bottom();
//
//translate([0, 0, 1.3]) rotate([0, 0, 0]) seal();
//seal2D();
//translate([0, 0, 1.3]) rotate([0, 0, 0]) seal_cutout();
//
translate([0, 0]) {
    latch(distance=-1.7); 
    translate([0, 14.5-1.7, 0]) latch_knob();
}
translate([13, 0]) mirror([1, 0]) {
    latch(distance=-1.7); 
    translate([0, 14.5-1.7, 0]) latch_knob();
}
//translate([20, 10]) rotate([90, 0, 0]) socket_normal(); 
//
translate([33, 20, 5.5]) rotate([90, 180, 0]) mirror([0, 0]) hinge_bottom(screwed=true);
translate([37, 48, 5.5]) rotate([90, 180, 0]) mirror([1, 0]) hinge_bottom(screwed=true);
//
//translate([20, 0, 0.3+12.5]) rotate([180, 0]) wedge();

//translate() rotate([0, 90, 0]) wedge_semiflex_motoZ();

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

module hex(size) {
    d=30;
    l=1.3;
    x=37.5-15;
    y = 21.65-8.65;
    h = 10;
 
    for(i = [0 : 3]) {
        translate([(2*x+2*l)*i, 0, -1]) {
            cylinder($fn=6, d=d, h=h);
            translate([0, y*2+l]) cylinder($fn=6, d=d, h=h);
            translate([0, -y*2-l]) cylinder($fn=6, d=d, h=h);
            translate([0, y*4+2*l]) cylinder($fn=6, d=d, h=h);
            translate([0, -y*4-2*l]) cylinder($fn=6, d=d, h=h);
            translate([x+l, y+l/2]) cylinder($fn=6, d=d, h=h);
            translate([x+l, -y-l/2]) cylinder($fn=6, d=d, h=h);
            translate([x+l, 3*(y+l/2)]) cylinder($fn=6, d=d, h=h);
            translate([x+l, 3*(-y-l/2)]) cylinder($fn=6, d=d, h=h);
        }
    }
} 

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
    tol = 0.25;
    height = 12.5;
    
    difference() {
        union() {
        translate([tol, 0.1+tol, tol]) cube([30.5-tol*2, 2.2-tol*2, height]);
        translate([2+tol, 2-tol*2-0.2, tol]) cube([26.5-tol*2, 2-tol*2+0.1, height]);
        
        points = [[-2, 0], [1, 0], [1, height], [-1.3-2.5, height]];
        translate([28.5-tol, 2.9, tol]) rotate([90, 0, -90]) linear_extrude(height=26.5-tol*2) polygon(points); 
        }
        
        translate([(30.5-10)/2, 4.5, .75-0.8]) cube([10, 10, 12]);
        translate([0, 0, height+tol]) rotate([-90, 0, -90]) linear_extrude(height=30.5) polygon([[-1-tol, 0], [0, 0], [0, 1+tol]]);
    }
}

module wedge_semiflex(height=20, hollow=true) {
    
    w = 0.9;
    
    points = [[3, 0], [10, 0], [10, 8.7], [10-1.3, 8.7+1.2], [10-1.3, 14], [10-1.3-0.9, 14], [10-1.3-0.9, 12],
    [3, 10], [4, 7]];
    
    points2 = [[3+w, w], [10-w, w], [10-w, 8.7-w], [10-1.3-w, 8.7+1.2-w], [10-1.3-0.9, 12-w], [3+w, 10-w/2], [4+w, 7]];
    
    difference() {
        translate([]) rotate([90, 0, -90]) linear_extrude(height=height) polygon(points);
        if (hollow) translate([+1, 0, 0]) rotate([90, 0, -90]) linear_extrude(height=height+2) polygon(points2);
    }
}

module wedge_semiflex_motoZ() {
    
    height=30;
    w = 0.9;
    
    points = [[4.9, 0], [10, 0], [10, 7.9], [10-2.3, 8.7+1.2], 
    [10-2.3, 12.5], [3, 15], [1.0, 14], [3.4, 10], [3.2, 6], [1.5, 0.9], [4.9, 1]];
    
//    points2 = [[3+w, w], [10-w, w], [10-w, 8.7-w], [10-1.3-w, 8.7+1.2-w], [10-1.3-0.9, 12-w], [3+w, 10-w/2], [4+w, 7]];
    
    difference() {
        translate([]) rotate([90, 0, -90]) linear_extrude(height=height) polygon(points);
//        translate([+1, 0, 0]) rotate([90, 0, -90]) linear_extrude(height=height+2) polygon(points2);
    }
    
//    translate([-height, -4.5, 12]) rotate([10, 0, 0]) cube([height, 0.9, 4]);
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

module socket_nut_wedge() {
    
    tol = 0.1;
    
    translate([-5.7+tol, -tol, +0.2]) difference() {
        translate([0, 0, -0.2]) cube([11.4-2*tol, 5.2-2*tol, 10]);
        translate([5.7, -1, 9-.2]) rotate([0, 90, 90]) cylinder($fn=6, h=10, d=13.2); 
    }
}

module seal() {
    height = 1;
    
    difference() {
        block2(sizeBot[0], sizeBot[1], height, crad=crad);    
        translate([0, 0, -1]) block2(sizeBot[0], sizeBot[1], height+2, crad=crad, red=4.5);  
    }     
}

module seal2D() {
    projection() seal();  
}

module seal_cutout() {
    height = 1;
    offset1 = 7.9;
    offset2 = 4.6;
    
    difference() { 
        union() {
            color("red") translate([0, 0]) linear_extrude(height=1) polygon([
                [offset1, offset2], [sizeBot[0]-offset1, offset2], 
                [sizeBot[0]-offset2, offset1], [sizeBot[0]-offset2, sizeBot[1]-offset1],
                [sizeBot[0]-offset1, sizeBot[1]-offset2], [offset1, sizeBot[1]-offset2], 
                [offset2, sizeBot[1]-offset1], [offset2, offset1]
            ]);
             
            translate([0, 0, 0]) block2(sizeBot[0], sizeBot[1], 3, crad=crad, red=6.5);  
        }
    
        translate([0, 0, -1]) block2(sizeBot[0], sizeBot[1], 10, crad=crad, red=9);  
    }
    
    translate([sizeBot[0]/5*1, 6.5]) cube([2, sizeBot[1]-6.5*2, 3]);
    translate([sizeBot[0]/5*2, 6.5]) cube([2, sizeBot[1]-6.5*2, 3]);
    translate([sizeBot[0]/5*3, 6.5]) cube([2, sizeBot[1]-6.5*2, 3]);
    translate([sizeBot[0]/5*4, 6.5]) cube([2, sizeBot[1]-6.5*2, 3]);
    
    translate([6.5, sizeBot[1]/2-2/2]) cube([sizeBot[0]-6.5*2, 2, 3]);
}


module top_flat() {
    
    a = 1;
    c = 3;
    d = 3;
    b = sizeTopF[2] - a - c - d - wb + .2;
    
    x = 1;
    y = 4.8;
    
    
    difference() {
        hull() {
            translate([0, 0, 0]) block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=x);
            translate([0, 0, a]) block2(sizeTopF[0], sizeTopF[1], 1.1, crad=crad);
        }
        translate([0, sizeTopF[1]/2]) hex();
    }
    
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
                    points_r = [[0, 0], [20, 0], [15, 5.5+1.3], [5, 5.5+1.3]];
                    translate([30, sizeTopF[1], 0]) rotate([0, 0, 180]) linear_extrude(height=sizeTopF[2]-seal_thickness/2) polygon(points_r);
                    translate([sizeTopF[0]+20-30, sizeTopF[1], 0]) rotate([0, 0, 180]) linear_extrude(height=sizeTopF[2]-seal_thickness/2) polygon(points_r);
                }
                
                // outer hull
                hull() {
                    translate([0, 0, 0]) block2(sizeTopF[0], sizeTopF[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) block2(sizeTopF[0], sizeTopF[1], sizeTopF[2]-a-seal_thickness/2, crad=crad);
                }
            }
            
            // seal nudge
            nudge_height = 0.3;
            translate([0, 0, sizeTopF[2]-seal_thickness/2]) difference() {
                block2(sizeTopF[0], sizeTopF[1], nudge_height, crad=crad, red=y-1.5-0.8-.1);
                translate([0, 0, -1]) block2(sizeTopF[0], sizeTopF[1], nudge_height+2, crad=crad, red=y-1.5);
            }
        }
        
        // rotation clamp holes
        if (screw_inset) {           
            translate([20, sizeTopF[1]+5, 5])               rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
            translate([sizeTopF[0]-20, sizeTopF[1]+5, 5])   rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
            translate([20, sizeTopF[1]-2, 5])               rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
            translate([sizeTopF[0]-20, sizeTopF[1]-2, 5])   rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
        } 
        if (heat_inset) {
          translate([20, sizeTopF[1]+1, 5])                 rotate([90, 0, 0]) cylinder($fn=32, h=5.5+1, d=heat_inset_diam);
          translate([sizeTopF[0]-20, sizeTopF[1]+1, 5])     rotate([90, 0, 0]) cylinder($fn=32, h=5.5+1, d=heat_inset_diam);
        }
    }
    
    // hinges
    intersection() {
        union() {
            move = 0.1;
            translate([31.2, -5.5, sizeTopF[2]+move])             rotate([0, -90, 0]) hinge_top();
            translate([sizeTopF[0]-15, -5.5, sizeTopF[2]+move]) rotate([0, -90, 0]) hinge_top();
            translate([16.3, 0]) cube([13.6, 2, 2]);
            translate([sizeTopF[0]-30+.1, 0]) cube([13.6, 2, 2]);
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
                    color("red") cavity(20);
                    
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
            
            // reinforcement
            intersection() {
                union() {
                    points_r = [[0, 0], [20, 0], [15, 5.5+1.3], [5, 5.5+1.3]];
                    
                    // hinge fastener 
                    translate([21, sizeBot[1], 1.2]) rotate([0, 0, 180]) linear_extrude(height=sizeBot[2]-1.2-seal_thickness/2) polygon(points_r);
                    
                    // rotation knob
                    translate([10, 0, 1.2]) rotate([0, 0, 0]) linear_extrude(height=sizeBot[2]-1.2-seal_thickness/2) polygon(points_r); 
                }
                block2(sizeBot[0], sizeBot[1], sizeBot[2], crad=crad);
            }
            
            // tripod screw holder
            translate([sizeBot[0]/2-45/2, +1]) hull() {
                points_h = [[0, 0], [2+1, -5], [45-(2+1), -5], [45, 0]];
                rotate([]) linear_extrude(height=18) polygon(points_h);
                
                a=1.5;
                translate([a/2, -1, 18+2-seal_thickness/2-.1]) cube([45-a, 0.1, 0.1]);
            }
        }
        
        // camera lens
        translate([lensHole[0], lensHole[1], -1]) {
            down = 39.4+0.5;
            up   = 36.9+0.3;
            cylinder($fn=64, h=1+3.60+0.3, d=down);
            translate([0, 0, 1+3.60+0.3-0.1]) cylinder($fn=64, h=2, d1=down, d=up);
            translate([0, 0, 6-.3]) cylinder($fn=64, h=2, d=up);
            translate([0, 0, 6+2-.3-.1]) cylinder($fn=64, h=3, d1=up, d2=34);
        }
        
        // socket screw holes
//        translate([sizeBot[0]/2, sizeBot[1]+5, 11.5]) rotate([90, 0, 0]) {
//            translate([-14, 0, 0]) cylinder($fn=32, h=50, d=5.3);
//            translate([+14, 0, 0]) cylinder($fn=32, h=50, d=5.3);
//        }
//        translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 11.5]) rotate([90, 0, 0]) { 
//            translate([-14, 0, 0]) cylinder($fn=6, h=50, d=9.6);
//            translate([+14, 0, 0]) cylinder($fn=6, h=50, d=9.6);
//        }
        
        // tripod socket holes
        translate([sizeBot[0]/2, -4+(4*0.4+0.1), -1]) rotate([0, 90, 90]) hull() {
            depth = 4.7+0.5;
            cylinder($fn=6, h=depth, d=13.2); 
            translate([-9-1-0.2, 0]) cylinder($fn=6, h=depth, d=13.2); 
        }
        color("red") translate([sizeBot[0]/2, -6.7+1, 9]) rotate([0, 90, 90]) cylinder($fn=32, h=10, d=7);
        
        // rotation clamp holes
        if (screw_inset) {
            translate([20, sizeBot[1]+10, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
            translate([20, sizeBot[1]-2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
            translate([sizeBot[0]-20, sizeBot[1]+10, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=50, d=3.3);
            translate([sizeBot[0]-20, sizeBot[1]-2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=50, d=6.6);
        }
        if (heat_inset) {
            translate([20, 5.5-.1, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=5.5, d=heat_inset_diam);     
            translate([sizeBot[0]-20, 5.5-.1, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=5.5, d=heat_inset_diam); 
        }
        
        // hinge holes
        hinge_hole_height = 11.5;
        if (screw_inset) {
            translate([10, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
            translate([10, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
            translate([10+26, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
            translate([10+26, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
            
            translate([sizeBot[0]-10, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
            translate([sizeBot[0]-10, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
            translate([sizeBot[0]-10-26, 20, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=50);
            translate([sizeBot[0]-10-26, 15+3.5, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=15);
        }
        if (heat_inset) {
            translate([10.2, sizeBot[1]+.1, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, h=5.5, d=heat_inset_diam);
            translate([10.2+26, sizeBot[1]+.1, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, h=5.5, d=heat_inset_diam);
            translate([sizeBot[0]-10.2, sizeBot[1]+.1, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, h=5.5, d=heat_inset_diam);
            translate([sizeBot[0]-10.2-26, sizeBot[1]+.1, hinge_hole_height]) rotate([90, 0, 0]) cylinder($fn=32, h=5.5, d=heat_inset_diam);
        }
        
       
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
//    % translate([9+06, -5.5-0.1, 20])                                 rotate([0, 90, 0]) color("purple") hinge_bottom(screwed=true);
//    % translate([sizeBot[0]-25-06+16, -5.5-0.1, 20])   mirror([1, 0]) rotate([0, 90, 0]) color("purple") hinge_bottom(screwed=true);
    % translate([25+6+0.2, sizeBot[1]+5.5+0.1, 20])                           rotate([0, 90, 180]) color("green") hinge_bottom(screwed=true);
    % translate([sizeBot[0]-25-6-0.2, sizeBot[1]+5.5+0.1, 20]) mirror([1, 0]) rotate([0, 90, 180]) color("green") hinge_bottom(screwed=true);
    
    // nuts
    % translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 11.5]) rotate([90, 0, 0]) { 
        translate([-14, 0, 0]) M5nut();
        translate([+14, 0, 0]) M5nut();
    }
    
    translate([36, 5, 11.5]) rotate([90, 0, 0]) M3nut();
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
