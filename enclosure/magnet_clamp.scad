% translate([0, 0, -7]) color("grey") {
    rotate([0, 0, 90+120]) translate([25, 0, 0]) magnet(); 
    rotate([0, 0, 90+240]) translate([25, 0, 0]) magnet(); 
    rotate([0, 0, 90+000]) translate([25, 0, 0]) magnet(); 
}

baseplate();

module baseplate() {
    height = 6;

    x = 21.7;
    y = 12.5;
    y2 = 25;
        
    y_head = 0;

    difference() {
        union() {
            translate([x, -y, 0]) cylinder($fn=64, d=20, h=height);
            translate([-x, -y, 0]) cylinder($fn=64, d=20, h=height);
            translate([0, y2, 0]) cylinder($fn=64, d=20, h=height);
            
            translate([x, -y, height]) cylinder($fn=64, d1=20, d2=20-2, h=2);
            translate([-x, -y, height]) cylinder($fn=64, d1=20, d2=20-2, h=2);
            translate([0, y2, height]) cylinder($fn=64, d1=20, d2=20-2, h=2);
            
            hull() {
                translate([x, -y, 0]) cylinder($fn=32, d=20-7, h=height-1);
                translate([-x, -y, 0]) cylinder($fn=32, d=20-7, h=height-1);
                translate([0, y2, 0]) cylinder($fn=32, d=20-7, h=height-1);
            }
            hull() {
                translate([x, -y, height-1]) cylinder($fn=32, d1=20-7, d2=20-8, h=1);
                translate([-x, -y, height-1]) cylinder($fn=32, d1=20-7, d2=20-8, h=1);
                translate([0, y2, height-1]) cylinder($fn=32, d1=20-7, d2=20-8, h=1);
            }
            
//            hull() {
//                translate([x, -y, height]) cylinder($fn=32, d1=6, d2=2, h=2);
//                translate([0, 0, height]) cylinder($fn=32, d1=6, d2=2, h=2);
//            }
//            hull() {   
//                translate([-x, -y, height]) cylinder($fn=32, d1=6, d2=2, h=2);
//                translate([0, 0, height]) cylinder($fn=32, d1=6, d2=2, h=2);
//            }
//            hull() {  
//                translate([0, y2, height]) cylinder($fn=32, d1=6, d2=2, h=2);
//                translate([0, 0, height]) cylinder($fn=32, d1=6, d2=2, h=2);
//            }
            
            // ballhead
            translate([0, y_head, 0]) cylinder($fn=64, d=29+6, h=height+3);
            translate([0, y_head, height+3]) cylinder($fn=64, d1=29+6, d2=29+6-2, h=1);
            
            // grappling hook
            translate([0, -20, 1]) hull() {
                translate([-12, 0, 0]) cylinder($fn=32, d=10, h=height-2);
                translate([+12, 0, 0]) cylinder($fn=32, d=10, h=height-2);
            }
            translate([0, -20, height-1]) hull() {
                translate([-12, 0, 0]) cylinder($fn=32, d1=10, d2=8, h=1);
                translate([+12, 0, 0]) cylinder($fn=32, d1=10, d2=8, h=1);
            }
            translate([0, -20, 0]) hull() {
                translate([-12, 0, 0]) cylinder($fn=32, d1=8, d2=10, h=1);
                translate([+12, 0, 0]) cylinder($fn=32, d1=8, d2=10, h=1);
            }
        }
        
        // ballhead screw hole, 1/4 inch
        translate([0, y_head, -1]) cylinder($fn=32, h=height+10, d=6.4+0.3); 
        translate([0, y_head, -1]) cylinder($fn=32, h=3, d=9.5+0.1);
        
        // ballhead hole
        translate([0, y_head, height]) cylinder($fn=64, d=29+0.3, h=height+2);
        
        // grappling hook
        translate([0, -20, -1]) hull() {
            translate([-10, 0, 0]) cylinder($fn=32, d=3, h=height+2);
            translate([+10, 0, 0]) cylinder($fn=32, d=3, h=height+2);
        }
        translate([0, -20, -1]) hull() {
            translate([-10, 0, height]) cylinder($fn=32, d1=3, d2=5, h=1.1);
            translate([+10, 0, height]) cylinder($fn=32, d1=3, d2=5, h=1.1);
        }
        translate([0, -20, -.1]) hull() {
            translate([-10, 0, 0]) cylinder($fn=32, d1=5, d2=3, h=1.1);
            translate([+10, 0, 0]) cylinder($fn=32, d1=5, d2=3, h=1.1);
        }
        
        // magnet screws
        translate([0, 0, -1]) {
            translate([x, -y, 0]) cylinder($fn=32, d=5.3, h=height+2);
            translate([-x, -y, 0]) cylinder($fn=32, d=5.3, h=height+2);
            translate([0, y2, 0]) cylinder($fn=32, d=5.3, h=height+2);
        }
        translate([0, 0, 4]) {
            translate([x, -y, 0]) cylinder($fn=6, d=9.6, h=height+2);
            translate([-x, -y, 0]) cylinder($fn=6, d=9.6, h=height+2);
            translate([0, y2, 0]) cylinder($fn=6, d=9.6, h=height+2);
        }
    }
    
    // ballhead screw support
    translate([0, y_head, 2]) cylinder(d=10, h=0.3);
}

module magnet() {
    difference() {
        union() {
            translate([]) cylinder($fn=32, h=5, d=36);
            translate([0, 0, 5]) cylinder($fn=32, h=2, d1=36, d2=36-2);
        }
        
        translate([0, 0, -1]) cylinder($fn=32, d=5, h=10);
    }
}