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

translate([sizeBot[0]/2, -0.1, 32]) rotate([90, 0, 0]) color("grey") grabber();
translate([sizeBot[0]/2, -0.1, 5]) rotate([90, 0, 0]) latcher();
translate([sizeBot[0]/2, -3.5, 38]) rotate([90, 0, 0]) latch();
translate([sizeBot[0]/2, -3.5, -2]) rotate([90, 0, 0]) latch2();


translate([130, 5, -3]) grabber();
translate([130, 20, -3]) latcher();
translate([130, 54, 0]) latch(print=true);
translate([145, 30, 0]) latch2();

module latch2() {
    difference() {
        union() {
            hull() {
                translate([-7+latchTol*2, 0, 0]) rotate([0, 90, 0]) cylinder($fn=32, d=6, h=14-latchTol*4);
                translate([-7+latchTol*2, 6, -3]) cube([14-latchTol*4, 0.1, 6]);
            }
            
            hull() {
                translate([-7+latchTol*2, 13, 0]) rotate([0, 90, 0]) cylinder($fn=32, d=6, h=4-latchTol*3);
                translate([-7+latchTol*2, 5, -3]) cube([4-latchTol*3, 0.1, 6]);
            }
            
            hull() {
                translate([3+latchTol, 13, 0]) rotate([0, 90, 0]) cylinder($fn=32, d=6, h=4-latchTol*3);
                translate([3+latchTol, 5, -3]) cube([4-latchTol*3, 0.1, 6]);
            }
        }
        
        translate([+10, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
     
        translate([+10, 12.5, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=2);
        
    }
}

module latch(print=false) {
    % translate([12.5, 0, 0]) rotate([0, -90, 0]) DIN912screw(20);
    % translate([12.5, -40, 0]) rotate([0, -90, 0]) DIN912screw(20);
    
    // translate([-10, 0, -10]) rotate([0, 90, 0]) 
    difference() {
        union() {
            hull() {
                translate([-9+2, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=6, h=2);
                translate([-9+2, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=6, h=2);
            }
         
            hull() {
                translate([-9-1.5, -6, -3]) cube([1.5, 6, 6]);
                translate([-9, -6-3, -3]) cube([0.1, 9, 6]);
            }
            
            hull() {
                translate([-9-1.5, -40, -3]) cube([1.5, 6, 6]);
                translate([-9, -40, -3]) cube([0.1, 9, 6]);
            }
        }
        translate([+10, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
        translate([+10, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
        
        translate([-9, 0, 0]) rotate([0, -90, 0]) cylinder($fn=6, h=10, d=6.6+.1);
        translate([-9, -40, 0]) rotate([0, -90, 0]) cylinder($fn=6, h=10, d=6.6+.1);
    }
    
    // translate([-18, 0, 6]) rotate([0, 90, 0]) 
    difference() {
        hull() {
            translate([+9, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=6, h=2);
            translate([+9, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, d=6, h=2);
        }    
        
        translate([+10, -40, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
        translate([+10, 0, 0]) rotate([0, -90, 0]) cylinder($fn=32, h=30, d=3.3);
    }
}

module latcher() {
    % translate([0, 12.5, 6.5]) rotate([180, 0, 0]) DIN912screw(8);
    
    difference() {
        union() {
            translate([-3, 0, 0]) block(6, 16, 3.5, crad=1);
            translate([-4, 16-6.75, 0]) block(8, 6.75, 3.5, crad=1);
            intersection() {
                translate([-3, 5, 3.5]) rotate([0, 90, 0]) cylinder($fn=32, d=6, h=6);
                translate([-3, 0, 0]) cube([6, 16, 10]);
            }
        }
        translate([-4, 5, 3.5]) rotate([0, 90, 0]) cylinder($fn=32, h=30, d=2);
        // translate([ 2.8, 5, 3.5]) rotate([0, 90, 0]) cylinder($fn=32, h=10, d=6);
        
        translate([0, 12.5, -1]) cylinder($fn=32, h=10, d=3.3);
        translate([0, 12.5, 3]) cylinder($fn=32, h=10, d=6);
    }
}

module grabber() {
    difference() {
            hull() {
                translate([-6+latchTol, 3.5, 4.7]) sphere($fn=32, d=2); //rotate([0, 90, 0]) cylinder($fn=32, d=2, h=14);
                translate([+6-+latchTol, 3.5, 4.7]) sphere($fn=32, d=2);
                translate([-7+latchTol, -4, 0]) block(14-latchTol*2, 8, 3, crad=1);
            }
        translate([0, 0, -1]) cylinder($fn=32, h=10, d=3.3);
        translate([0, 0, 1.4]) cylinder($fn=32, h=10, d=6);
    }
    
    % translate([0, 0, 4.5]) rotate([180, 0, 0]) DIN912screw(8);
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