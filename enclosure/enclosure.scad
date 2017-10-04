include <hinge.scad>

sizeBot    = [170, 86, 20];
sizeTop    = [170, 86, 23];
lensHole   = [sizeBot[0]-25, 25];

lidDepth   = 1;
lidWall    = 0.8;
lidTol     = 0.4;

crad       = 6;
w          = 3.2+.1;
wb         = 1.2;

//difference(){
//    union() {
//        bottom();
//        translate([0, sizeTop[1], 44-.7]) rotate([180, 0, 0]) top();
//    }
//    translate([-1, -1, -1]) cube([7, 100, 50]);
//}

top();

//bottom();
//translate([70, 4.5, 7+10]) color("green") wedge();
% translate([0, sizeBot[1]-2.3, 18.1]) rotate([0, 90, 0]) color([1, 1, 1], 1) cylinder($fn=32, d=1.75, h=100);

//difference(){
//    bottom();
//    translate([-1, -1, -1]) cube([15, 100, 50]);
//}

//intersection() {
//    bottom();
//    translate([lensHole[0], lensHole[1], -1]) cylinder($fn=32, d=48, h=10);
//}

//translate([0, 0, sizeBot[2]+5]) seal(); //color("green") seal();
//translate([0, 100-3+10, 0]) top();
% translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 3.5]) rotate([90, 0, 180]) socket();
//% translate([sizeBot[0]/2-(44/2), -30, 4]) rotate([0, 0, 0]) socket();
//% translate([63.5, -5-2, 30]) rotate([-90, 0, 0]) DIN912screw(8);

//% translate([30, 10, 5+0]) nexus5();

* translate([127, 40, -10]) uvfilter();
//% translate([5, 36, 4]) usbplug();

//translate([200, 0, 0]) {
//    bottom();
//    translate([0, sizeTop[1], 44.1]) rotate([180, 0, 0]) top();
//    translate([sizeBot[0]/2-(44/2)+44, sizeBot[1]+.1, 4]) rotate([90, 0, 180]) socket();
//    translate([55, 0, 44]) rotate([0, 180, 0]) color("green") import("hinge.stl");
//    translate([95+55, 0, 44]) rotate([0, 180, 0]) color("green") import("hinge.stl");
//}

//translate([30, 105, 4]) cube([126, 69, 14]); // jet
//translate([30, 105, 4]) cube([92, 59, 22]); // anker 10.000
//translate([30, 105, 4]) cube([98, 8, 22]); // anker 10.000
//% translate([25, 104, 4]) color("grey") block(142.3, 72.4, 17.5, crad=3); // anker 10.000


// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

module wedge() {
    tol = 0.2;
    height = 12.5;
    
    translate([tol, tol, tol]) cube([40-tol*2, 2-tol*2, height]);
    translate([2+tol, 2-tol, tol]) cube([36-tol*2, 2-tol*2, height]);
    
    points = [[0, 0], [1, 0], [1, height], [-3, height]];
    translate([38-tol, 1+4-tol*3, tol]) rotate([90, 0, -90]) linear_extrude(height=36-tol*2) polygon(points); 
}

module socket() {
    width = 44;
    depth = 16;
    difference() {
        block(width, depth, 7.5);
        
        translate([(width)/2-14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2+14, depth/2, -1]) cylinder($fn=32, h=20, d=5.3);
        translate([(width)/2-14, depth/2, 1.8]) cylinder($fn=32, h=20, d=9);
        translate([(width)/2+14, depth/2, 1.8]) cylinder($fn=32, h=20, d=9);
        
        translate([(width)/2, depth/2, -10]) cylinder($fn=32, h=20, d=7);
        translate([(width)/2, depth/2, -1]) cylinder($fn=6, h=1+6, d=13.15);  // +1 safety margin for long fastening screws
    }
    
    translate([(width)/2+14-5, depth/2-5, 1.8-.3]) cube([10, 10, .3]);
    translate([(width)/2-14-5, depth/2-5, 1.8-.3]) cube([10, 10, .3]);
}

module seal() {
    height = 2;
    sealTol = 0.6;
    
    difference() {
        union() {
            difference() {
                block(sizeBot[0], sizeBot[1], height, crad=crad);
                translate([0, 0, -1]) block(sizeBot[0], sizeBot[1], 1.2+2, crad=crad, red=1+w);
            }
            
            intersection() {
                union() {
                    cube([9, 12, height]);
                    cube([12, 9, height]);
                    translate([9, 9, 0]) cylinder($fn=32, r=3, h=height);
                    
                    translate([0, sizeBot[1]-12]) {
                        translate([0, 0, 0]) cube([9, 9, height]);
                        translate([0, 3, 0]) cube([12, 9, height]);
                        translate([9, 3, 0]) cylinder($fn=32, r=3, h=height);
                    }
                    
                    translate([sizeBot[0]-12, sizeBot[1]-12]) {
                        translate([0, 3, 0]) cube([9, 9, height]);
                        translate([3, 0, 0]) cube([9, 9, height]);
                        translate([3, 3, 0]) cylinder($fn=32, r=3, h=height);
                    }
                    
                    translate([sizeBot[0]-12, 0]) {
                        translate([0, 0, 0]) cube([9, 9, height]);
                        translate([3, 3, 0]) cube([9, 9, height]);
                        translate([3, 9, 0]) cylinder($fn=32, r=3, h=height);
                    }
                }
                
                block(sizeBot[0], sizeBot[1], height, crad=crad);
            }
            
            // center holes
            translate([0, 0, -1]) {
                translate([7, 7]) cylinder($fn=32, d=6-sealTol, h=2);
                translate([7, sizeBot[1]-7]) cylinder($fn=32, d=6-sealTol, h=2);
                translate([sizeBot[0]-7, 7]) cylinder($fn=32, d=6-sealTol, h=2);
                translate([sizeBot[0]-7, sizeBot[1]-7]) cylinder($fn=32, d=6-sealTol, h=2);
            }
            
            // TODO: add tolerance
            translate([0, 0, -0.8]) color("red") difference() {
                height_seal = 1;
                block(sizeTop[0], sizeTop[1], height_seal, crad=crad, red=0.8);
                translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height_seal+2, crad=crad, red=0.8+0.8);
            }
        }
        
        translate([0, 0, -3]) { 
            translate([7, 7]) cylinder($fn=32, d=3.3, h=30);
            translate([7, sizeBot[1]-7]) cylinder($fn=32, d=3.3, h=30);
            translate([sizeBot[0]-7, 7]) cylinder($fn=32, d=3.3, h=30);
            translate([sizeBot[0]-7, sizeBot[1]-7]) cylinder($fn=32, d=3.3, h=30);
        }
        
        // TODO: add tolerance
        translate([0, 0, 1.2]) color("red") difference() {
            height_seal = 2;
            block(sizeTop[0], sizeTop[1], height_seal, crad=crad, red=0.8);
            translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height_seal+2, crad=crad, red=0.8+0.8);
        }
    }
}

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
                height = 1.5;
                block(sizeTop[0], sizeTop[1], height, crad=crad, red=1.2+0.1+0.2); // = 1.5
                translate([0, 0, -1]) block(sizeTop[0], sizeTop[1], height+2, crad=crad, red=(1.5)+1.5+0.1);
            }
            
            //inlay
            intersection() {
                inlay = [154, 71.8, 23];
                inlayTol = 0.5;
                
                difference() {
                    translate([sizeTop[0]-150, 0, 0]) {
                        cube([150, sizeTop[1], 23]);
                    } 
                    
                    trans = [10, (sizeTop[1]-(inlay[1]+inlayTol*2))/2, 0];
                    translate(trans) block(inlay[0]+inlayTol*2, inlay[1]+inlayTol*2, inlay[2]+inlayTol*2, crad=3);
                
                    points = [[0, 0], [sizeTop[1], 0], [sizeTop[1], 20], [sizeTop[1]-20, 30], [20, 30], [0, 20]];
                    translate([0, sizeTop[1]]) rotate([0, 0, -90]) linear_extrude(height=30) polygon(points);
                }
                
                // outer hull
                hull() {
                    block(sizeTop[0], sizeTop[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block(sizeTop[0], sizeTop[1], sizeTop[2]-a, crad=crad);
                }
            }
        }
        
        // hinge holes
        translate([30, sizeBot[1]+5, 17.6]) rotate([90, 0, 0]) cylinder($fn=32, h=20, d=3.3);
        translate([sizeBot[0]-30, sizeBot[1]+5, 17.6]) rotate([90, 0, 0]) cylinder($fn=32, h=20, d=3.3);
        translate([30, sizeBot[1]-2, 17.6]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        translate([sizeBot[0]-30, sizeBot[1]-2, 17.6]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
    }
    
    translate([+20+18.2, -5.5, 23]) rotate([0, -90, 0]) hinge_top();
    translate([-20+sizeTop[0]-2, -5.5, 23]) rotate([0, -90, 0]) hinge_top();
    
}

module bottom() {   
    
    a = 2;
    c = 2;
    d = 4;
    b = sizeBot[2] - a - c - d - wb + .2;
    
//    z = 
    x = 2;
    y = 4.8;
    
    difference() {
        union() {
            difference() {
                
                hull() { // outer rim reduction
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=1);
                    translate([0, 0, 1]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-1, crad=crad);
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
            
            // inlay
            translate([0, 0, 0]) intersection() {
                difference() {
                    translate([20, w, 0]) cube([sizeBot[0]-20, sizeBot[1]-2*w, 20]); //20-1]);
                    translate([30, 10, 7]) color("red") nexus5cavity(20);
                    
                    points = [[0, 0], [sizeTop[1], 0], [sizeTop[1], 20], [sizeTop[1]-20, 30], [20, 30], [0, 20]];
                    translate([0, sizeTop[1]]) rotate([0, 0, -90]) linear_extrude(height=30) polygon(points);
                    
                    * hull() {
                        translate([30, 9, 15-.1]) color("red") nexus5cavity(0.1);
                        translate([30-1, w, 20-1]) cube([sizeBot[0]-19, sizeBot[1]-2*w, 1]);
                    }
                }
                hull() {
                    block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=x);
                    translate([0, 0, a]) 
                        block(sizeBot[0], sizeBot[1], sizeBot[2]-a, crad=crad);
                }
            }
            
            
            // hinge nut support
            translate([12, sizeBot[1]-4.8, 1.2]) cube([12, 4.8, 16]);
        }
        
        // camera lens
        translate([lensHole[0], lensHole[1], -1]) {
            down = 39.4+0.7;
            up   = 36.9+0.5;
            cylinder($fn=64, h=1+3.60+0.3, d=down);
            translate([0, 0, 1+3.60+0.3-0.1]) cylinder($fn=64, h=1, d1=down, d=up);
            translate([0, 0, 6-.3]) cylinder($fn=64, h=2, d=up);
            translate([0, 0, 6+2-.3-.1]) cylinder($fn=64, h=3, d1=up, d2=34);
        }
        
        // socket screw holes
        translate([sizeBot[0]/2, sizeBot[1]+5, 11.5]) rotate([90, 0, 0]) {
            translate([-14, 0, 0]) cylinder($fn=32, h=10, d=5.3);
            translate([+14, 0, 0]) cylinder($fn=32, h=10, d=5.3);
        }
        
        // hinge holes
        translate([30, 5, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
        translate([sizeBot[0]-30, 5, 12]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
        translate([30, 10+2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        translate([sizeBot[0]-30, 10+2.6, 12]) rotate([90, 0, 0]) cylinder($fn=6, h=10, d=6.6);
        
        translate([17.2, sizeBot[1]+1, 11.5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=10);
        translate([17.2, sizeBot[1]-3.5, 11.5]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=8);
        translate([43.2, sizeBot[1]+1, 11.5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=10);
        translate([43.2, sizeBot[1]-3.5, 11.5]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=8);
        
        translate([sizeBot[0]-43, sizeBot[1]+1, 11.5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=10);
        translate([sizeBot[0]-43, sizeBot[1]-3.5, 11.5]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=8);
        translate([sizeBot[0]-17, sizeBot[1]+1, 11.5]) rotate([90, 0, 0]) cylinder($fn=32, d=3.3, h=10);
        translate([sizeBot[0]-17, sizeBot[1]-3.5, 11.5]) rotate([90, 0, 0]) cylinder($fn=6, d=6.6, h=8);
    
        
        // socket holes
        translate([sizeBot[0]/2, sizeBot[1]-w-0.5, 11.5]) rotate([90, 0, 0]) { 
            translate([-14, 0, 0]) cylinder($fn=6, h=10, d=9.6);
            translate([+14, 0, 0]) cylinder($fn=6, h=10, d=9.6);
        }
        
        // seal
        translate([0, 0, sizeBot[2]-2.3]) color("red") difference() {
            height = 3;
            union() {
                hull() {
                    block(sizeBot[0], sizeBot[1], height-.3, crad=crad, red=1.3);
                    translate([0, 0, -.5]) block(sizeBot[0], sizeBot[1], height, crad=crad, red=1.3+.5);
                }
                
                translate([0, 0, height-0.7-0.3]) block(sizeBot[0], sizeBot[1], 1, crad=crad, red=1.3-.4);
            }
            
            translate([0, 0, -.1]) block(sizeBot[0], sizeBot[1], height+2, crad=crad, red=1.3+2);
            translate([0, 0, -.6]) color("orange") hull() {
                block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=1.3+2-0.5);
                translate([0, 0, 0.5])block(sizeBot[0], sizeBot[1], 0.1, crad=crad, red=1.3+2);
            }
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
//    translate([20+18.2, sizeBot[1]+5.5, 20]) rotate([0, 90, 180]) hinge_bottom(screwed=true);
//    translate([-20+sizeBot[0]-2, sizeBot[1]+5.5, 20]) rotate([0, 90, 180]) hinge_bottom(screwed=true);
    
    // hinge support
//    translate([17-.2, sizeBot[1], 2])                                   hinge_support();
//    translate([sizeBot[0]-30-13.4, sizeBot[1], 2])                      hinge_support();
//    translate([30+13.4, sizeBot[1], 14+2]) rotate([0, 180, 0])          hinge_support();
//    translate([sizeBot[0]-17+.2, sizeBot[1], 14+2]) rotate([0, 180, 0]) hinge_support();
    
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
    translate([65, -0.5, 7]) cube([17, 2, 1.5]);
    translate([65+17+10, -1, 7]) cube([9, 2, 1.5]);
    translate([130-23, 67/2, -1]) cylinder($fn=32, h=3, d=13);
}

module nexus5() {
    translate([-5, 68, -26]) rotate([0, 0, -90]) import("nexus5.stl");
}

module nexus5cavity(height) {

    difference() {
        union() {
            color("red") {
                translate([124, 61+1, 0]) cylinder($fn=32, h=height, d=14);
                translate([124, 5,  0]) cylinder($fn=32, h=height, d=14);
            }
            color("green") intersection() {
                translate([120, 5, 0]) cube([20, 56+1, height+2]);
                translate([124.8, (56+1)/2+5,  0]) scale([0.18, 1]) cylinder($fn=32, h=height, d=90);
            }
            
            translate([-11, -2, 0]) cube([135, (56+1)+14, height]);
        }
    
        points_cav = [[0, 0], [-2, height+0.2], [2, height+0.2], [2, 0]];
        translate([-12, 68-.1, -.1]) rotate([90, 0, 90]) linear_extrude(height=200) polygon(points_cav);
    }
    
    
    translate([102, -4.9, 0])cube([16, 5, height]);
    translate([82, 65, 0])cube([26, 5, height]);
    
    // spring
    translate([40, -5.5, 0]) cube([40, 2, height]);
    translate([40+2, -5.5, 0]) cube([36, 5, height]);
    
    

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