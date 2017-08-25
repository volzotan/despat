sizeBot    = [50, 30, 17];
sizeTop    = [50, 30, 17];

lidDepth   = 1;
lidWall    = 0.8;
lidTol     = 0.4;
hingeTol   = 0.3;

crad       = 6;
w          = 2.0;
wb         = 1.2;

bottomRounding = 2;
bottomReduction = 1;

bottom();

move = 3.1;

translate([0, sizeTop[1], 33+move]) rotate([180, 0, 0]) top();

translate([33, 36.5, 17]) rotate([90, 90, -90]) hinge_bottom();
translate([33, 36.5, 17+move]) rotate([90, 90, -90]) hinge_top();

translate([100, 0, 0]) hinge_bottom();
translate([100, 0, 0]) hinge_top();

translate([sizeBot[0]/2, -0.1, 24.4]) rotate([90, 0, 0]) color("grey") grabber();

module grabber() {
    //difference() {
        //intersection() {
            translate([-7, -4, 0]) block(14, 8, 4, crad=1);
            
            hull() {
                translate([-7, 4, 4]) rotate([0, 90, 0]) cylinder($fn=32, d=2, h=14);
                translate([-7, -4, 0]) cube([14, 8, 3]);
            }
        //}
        translate([0, 0, -1]) cylinder($fn=32, h=10, d=3.3);
        translate([0, 0, 1.4]) cylinder($fn=32, h=10, d=6);
    //}
}

module hinge_bottom() {
    diam = 10;
    
    difference() { 
        union() {
            translate([0, 0, -2]) cylinder($fn=32, d=diam, h=3+2);
            translate([0, 0, 3*2 + hingeTol*2]) cylinder($fn=32, d=diam, h=3);
            translate([0, 0, 3*4 + hingeTol*4]) cylinder($fn=32, d=diam, h=3+2);
              
            color("red") translate([0, 0, -2]) linear_extrude(height=2*2 + 5*3 + hingeTol*4) polygon(points);
            points = [[0, diam/2], [4.25, -4.25], [14, 6.5], [0.5, 6.5]];          
        }
        
        translate([0, 0, 3]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        translate([0, 0, 3*3+hingeTol*2]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        
        translate([0, 0, -1]) cylinder($fn=32, h=30, d=3.5); // diameter slightly increased
        translate([0, 0, -5]) cylinder($fn=32, h=5, d=6);
        translate([0, 0, 16]) cylinder($fn=32, h=5, d=7);
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
            points = [[0, diam/2], [-4.25, -4.25], [-14, 6.5], [-0.5, 6.5]];  
        }
        
        translate([0, 0, 3*2+hingeTol]) cylinder($fn=32, d=diam+2, h=3+hingeTol*2);
        
        translate([0, 0, -1]) cylinder($fn=32, h=30, d=3.5); // diameter slightly increased
        translate([0, 0, -5]) cylinder($fn=32, h=5, d=6);
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


module DIN912screw(length) {
    difference() {
        union() {
            cylinder($fn=32, h=3, d=5.5);
            translate([0, 0, 3]) cylinder($fn=32, h=length, d=3);
        }
        translate([0, 0, -1]) cylinder($fn=6, h=3, d=5);
    }
}