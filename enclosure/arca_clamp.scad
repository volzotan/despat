translate([-3, 06, 1]) clamp();
% translate([0, 0, -1]) color("grey") arca();

module arca() {
    difference() {
        block(39, 16, 50, crad=0.5);
    
        points = [[0, 0], [4, 0], [0, 4]];
        translate([-0.01, 16-5, -10]) rotate([]) linear_extrude(height=70) polygon(points);
        translate([39+0.01, 16-5, -10]) rotate([]) mirror([1, 0]) linear_extrude(height=70) polygon(points);
    }
}

module clamp() {
    
    difference() {
        translate([0, 3]) block(39+6, 12, 24, crad=2);
        translate([3-.4, 0, -1]) cube([39.8, 10.4, 100]);
        
        translate([45/2, 20, 24/2]) rotate([90, 0, 0]) cylinder($fn=32, d=6.35+0.5, h=20);
        
        translate([45/2-(16+1)/2, 13.5, -1]) cube([16+1, 10, 100]);
    }
    
    points = [[0, 0], [4, 0], [0, 4]];
    translate([3-1, 5+0.4]) rotate([]) linear_extrude(height=24) polygon(points);
    translate([39+4-0, 5+0.4]) rotate([]) mirror([1, 0]) linear_extrude(height=24) polygon(points);
    
}

module block(width, depth, height, crad=3, red=0) {
    hull() {    
        translate([crad, crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([width-crad, crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([crad, depth-crad]) cylinder($fn=32, h=height, r=crad-red);
        translate([width-crad, depth-crad]) cylinder($fn=32, h=height, r=crad-red);
    }
}