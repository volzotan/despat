var session = {
    "camera": {
        "1": {
            "name": "Moto Z",
            "position": [50.971038, 11.038084]
        },
    }

};

var pi = Math.PI,
    tau = 2 * pi;

var svg = d3.select("svg"),
    width = +svg.attr("width"),
    height = +svg.attr("height")
    // margin = {top: 20, right: 20, bottom: 30, left: 40},
    // g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    layer_map = svg.append("g");
    layer_hex = svg.append("g");
    // layer_sca = svg.append("g");
    layer_sca = d3.select("canvas").node().getContext("2d");
    layer_sym = svg.append("g");

console.log(layer_sca);

var projection = d3.geoMercator()
    .scale((1 << 8 + 19) / tau)
    .translate([width / 2, height / 2])
    .center([11.037630, 50.971296]);

var path = d3.geoPath()
    .projection(projection);

var color = d3.scaleSequential(d3.interpolateLab("white", "steelblue"))
    .domain([0, 70]);

var alpha = d3.scaleLinear()
    .domain([0, 70])
    .range([0.2, 1.0]);

var hexbin = d3.hexbin()
    .radius(5)
    .extent([[0, 0], [width, height]]);

var symbols_data = [
    ["C", "Camera", [50.971038, 11.038084], "red"]
];

d3.text("boxes.txt", function(error, raw) {
    if (error) throw error;

    // data:
    // timestamp device class confidence lat lon minx miny maxx maxy

    var boxes = d3.dsvFormat("|").parseRows(raw);
        boxesRaw = [];
        boxes.forEach((box, index) => {
            var coord = projection([box[5], box[4]]);
            box[5] = coord[0];
            box[4] = coord[1];
            boxesRaw.push(coord);
            return boxes[index] = box; // lonlat!
        });

    console.log(boxes[0])

    var tiles = d3.tile()
        .size([width, height])
        .scale(projection.scale() * tau)
        .translate(projection([0, 0]))
        ();

    layer_map
            .attr("class", "layer_map")
        .selectAll("image")
        .data(tiles)
        .enter().append("image")
            .attr("xlink:href", getImg)
            .attr("filter", "url(#grayscale)")
            .attr("x", function(d) { return (d[0] + tiles.translate[0]) * tiles.scale; })
            .attr("y", function(d) { return (d[1] + tiles.translate[1]) * tiles.scale; })
            .attr("width", tiles.scale)
            .attr("height", tiles.scale);

    layer_hex
            .attr("class", "layer_hex")
        .append("g")
            .attr("class", "hexagon")
            .attr("clip-path", "url(#clip)")
        .selectAll("path")
        .data(hexbin(boxesRaw))
        .enter().append("path")
            .attr("d", hexbin.hexagon())
            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
            .attr("fill", function(d) { return color(d.length); })
            .attr("fill-opacity", function(d) { return alpha(d.length); });

    // layer_sca.append("g")
    //     .selectAll(".dot")
    //     .data(boxes)
    //     .enter().append("circle")
    //         .attr("class", "dot")
    //         .attr("r", 0.5)
    //         .attr("cx", function(d) { return d[5]; })
    //         .attr("cy", function(d) { return d[4]; })
    //         .attr("fill-opacity", 0.3)
    //         .style("fill", "black");

    layer_sca.draw = function() {
        layer_sca.clearRect(0, 0, width, height);
        boxes.forEach((box, index) => {
            x = box[5];
            y = box[4];

            layer_sca.beginPath();
            layer_sca.fillStyle = "#333";
            layer_sca.strokeStyle = "#333";
            layer_sca.arc(x, y, 0.5, 0, 2 * Math.PI);
            // layer_sca.stroke();
            layer_sca.fill();
            layer_sca.closePath();

        });
    };
    layer_sca.draw();

    var symbols = layer_sym
        .attr("class", "layer_sym")
        .selectAll(".symbol")
        .data(symbols_data)
        .enter()
        .append("g")
            .attr("transform", function(d) {
                var coords = projection([d[2][1], d[2][0]]);
                console.log(coords);
                return "translate(" + coords[0] + "," + coords[1] + ")";
            });

    symbols.append("circle")
        .attr("class", "sym")
        .attr("r", 10)
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("fill-opacity", "1")
        .style("fill", function(d) { return d[3]; });

    symbols.append("text")
        .attr("height", 10)
        .attr("width", 10)
        .attr("transform", "translate(" + -6 + "," + +5 + ")")
        .text(function(d) { return d[0] });

});

function getImg(d) {

    console.log(d[2]);

    var osmTilename = d[2] + "/" + d[0] + "/" + d[1] + ".png";

    // var url = "http://" + "abc"[getRandomInt(0, 2)] + ".tile.openstreetmap.org/" + osmTilename;
    // var url = "http://a.tile.stamen.com/toner/" + osmTilename;
    // var url = "tiles/" + osmTilename;
    // var url = "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "s" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];

    var url = "tiles/" + "gmaps_satellite" + "/" + osmTilename;

    return url;
}