sizeBot    = [50, 30, 14];
sizeTop    = [50, 30, 15];

lidDepth   = 1;
lidWall    = 0.8;
lidTol     = 0.4;
hingeTol   = 0.3;

crad       = 6;
w          = 2.0 + 0.1;
wb         = 1.2;

bottomRounding = 1;
bottomReduction = 1;

latchTol = 0.5;

move = .1;

// print

//bottom();
//translate([33, 35.5, 14]) rotate([90, 90, -90]) hinge_bottom();
//
//translate([0, 41, 0]) { //translate([-70, 30, 28.1]) rotate([180, 0, 0]) {
//    top();
//    translate([33, -5.5, 14]) rotate([0, -90, 0]) hinge_top();
//}


//color("green") bottom();
//color("green") translate([0, sizeTop[1], 29+move]) rotate([180, 0, 0]) top();
//
//translate([33, 35.5, 14]) rotate([90, 90, -90]) hinge_bottom();
//translate([33, 35.5, 14+move]) rotate([90, 90, -90]) hinge_top();

//translate([100, 0, 0]) hinge_bottom();
//translate([100, 0, 0]) hinge_top();

//translate([sizeBot[0]/2, -0.1, 32]) rotate([90, 0, 0]) color("grey") grabber();
//translate([sizeBot[0]/2, -0.1, 5]) rotate([90, 0, 0]) latcher();
//translate([sizeBot[0]/2, -3.5, 38]) rotate([90, 0, 0]) latch();
//translate([sizeBot[0]/2, -3.5, -2]) rotate([90, 0, 0]) latch2();


translate([116, 2, -3.5]) grabber();
translate([130, 0, 0.5]) rotate([0, 90, 0]) latcher();
translate([130, 54, 0]) latch(print=true);
translate([135, 30, 0]) latch2();

module latch2() {
    difference() {
        union() {
            hull() {
                translate([-10+latchTol*2, 0, 0]) rotate([0, 90, 0]) cylinder($fn=32, d=7, h=20-latchTol*4);
                translate([-10+latchTol*2, 5, -3.5]) cube([20-latchTol*4, 0.1, 7]);
            }
            
            hull() {
                translate([-10+latchTol*2, 13, 0]) rotate([0, 90, 0]) cylinder($fn=32, d=7, h=6-latchTol*3);
                translate([-10+latchTol*2, 5, -3.5]) cube([6-latchTol*3, 0.1, 7]);
            }
            
            hull() {
                translate([4+latchTol, 13, 0]) rotate([0, 90, 0]) cylinder($fn=32, d=7, h=6-latchTol*3);
                translate([4+latchTol, 5, -3.5]) cube([6-latchTol*3, 0.1, 7]);
            }
        }
        
        translate([+10, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
     
        translate([+10, 12.5, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=2.3);
        translate([+10+1, 12.5, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=2+1.3, d=5);
        
    }
}

module latch(print=false) {
    % translate([15, 0, 0]) rotate([0, -90, 0]) DIN912screw(25);
    % translate([15, -40, 0]) rotate([0, -90, 0]) DIN912screw(25);
    
    translate([-10, 0, -13]) rotate([0, 90, 0]) 
    difference() {
        union() {
            hull() {
                translate([-11.5+2, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=7, h=2);
                translate([-11.5+2, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=7, h=2);
            }
         
            hull() {
                translate([-11.5-1.5, -6, -3.5]) cube([1.5, 6, 7]);
                translate([-11.5, -6-3, -3.5]) cube([0.1, 9, 7]);
            }
            
            hull() {
                translate([-11.5-1.5, -40, -3.5]) cube([1.5, 6, 7]);
                translate([-11.5, -40, -3.5]) cube([0.1, 9, 7]);
            }
        }
        translate([+10, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
        translate([+10, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
        
        translate([-11.5, 0, 0]) rotate([0, -90, 0]) cylinder($fn=6, h=10, d=6.6+.1);
        translate([-11.5, -40, 0]) rotate([0, -90, 0]) cylinder($fn=6, h=10, d=6.6+.1);
    }
    
    translate([-18, 0, 8]) rotate([0, 90, 0]) 
    difference() {
        hull() {
            translate([+11.5, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=7, h=2);
            translate([+11.5, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=7, h=2);
        }    
        
        translate([+15, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
        translate([+15, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
    }
}

module latcher() {
    % translate([0, 12.5, 6.5]) rotate([180, 0, 0]) DIN912screw(8);
    
    difference() {
        union() {
            translate([-4, 0, 0]) block(8, 16, 3.5, crad=1);
            
            translate([-4, 1, 0]) cube([8, 8, 4.5]);
            intersection() {
                translate([-4, 5, 4.5]) rotate([0, 90, 0]) cylinder($fn=32, d=8, h=8);
                translate([-4, 0, 0]) cube([8, 16, 10]);
            }
        }
        
        translate([-5, 5, 4.5]) rotate([0, 90, 0]) cylinder($fn=32, h=30, d=2.3);
        // translate([ 2.8, 5, 3.5]) rotate([0, 90, 0]) cylinder($fn=32, h=10, d=6);
        
        translate([0, 12.5, -1]) cylinder($fn=32, h=10, d=3.3);
        translate([0, 12.5, 3]) cylinder($fn=32, h=10, d=6);
    }
}

module grabber() {
    difference() {
            hull() {
                translate([-8+latchTol, 4, 5.3]) sphere($fn=32, d=2); //rotate([0, 90, 0]) cylinder($fn=32, d=2, h=14);
                translate([+8-+latchTol, 4, 5.3]) sphere($fn=32, d=2);
                translate([-9+latchTol, -4, 0]) block(18-latchTol*2, 8, 4, crad=1);
            }
        translate([0, 0, -1]) cylinder($fn=32, h=10, d=3.3);
        translate([0, 0, 2.4]) cylinder($fn=32, h=10, d=6);
    }
    
    % translate([0, 0, 5.5]) rotate([180, 0, 0]) DIN912screw(8);
}

module hinge_bottom() {
    diam = 10;
    
    difference() { 
        union() {
            translate([0, 0, -2]) cylinder($fn=32, d=diam, h=3+2);
            translate([0, 0, 3*2 + hingeTol*2]) cylinder($fn=32, d=diam, h=3);
            translate([0, 0, 3*4 + hingeTol*4]) cylinder($fn=32, d=diam, h=3+2);
              
            color("red") translate([0, 0, -2]) linear_extrude(height=2*2 + 5*3 + hingeTol*4) polygon(points);
            points = [[0, diam/2], [3.54, -3.54], [13, 5.5], [0.5, 5.5]];          
        }
        
        translate([0, 0, 3]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        translate([0, 0, 3*3+hingeTol*2]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        
        translate([0, 0, -1]) cylinder($fn=32, h=30, d=3.5); // diameter slightly increased
        translate([0, 0, -5]) cylinder($fn=32, h=5, d=6);
        translate([0, 0, 16]) cylinder($fn=6, h=5, d=6.6);
        
        translate([0, -6, 3]) cube([10, 7.5, 3+hingeTol*2]);
        translate([0, -6, 3*3+hingeTol*2]) cube([10, 7.5, 3+hingeTol*2]);
    }
    
    % translate([0, 0, -3.2]) DIN912screw(20);
}

module hinge_top() {
    diam = 10;
    
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

module top() {
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
            
            
            translate([sizeBot[0]/2, 35, 8]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
            }
}

module bottom() {
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
            //}  
            translate([sizeBot[0]/2, 5, 8]) rotate([90, 0, 0]) cylinder($fn=32, h=10, d=3.3);
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
        translate([crad, crad]) cylinder($fn=64, h=height, r=crad-red);
        translate([width-crad, crad]) cylinder($fn=64, h=height, r=crad-red);
        translate([crad, depth-crad]) cylinder($fn=64, h=height, r=crad-red);
        translate([width-crad, depth-crad]) cylinder($fn=64, h=height, r=crad-red);
    }
}


module DIN912screw(length) {
    difference() {
        union() {
            cylinder($fn=32, h=3, d=5.5);
            translate([0, 0, 3]) cylinder($fn=32, h=length, d=3);
        }
        translate([0, 0, -1]) cylinder($fn=6, h=3, d=4);
    }
}