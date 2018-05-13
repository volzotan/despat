var dataset = {

    "cameras": [
        {
            "id": "1",
            "name": "Moto Z",
            "position": [50.971040, 11.038093]
        },
        {
            "id": "2",
            "name": "ZTE",
            "position": [50.970930, 11.038083]
        }
        ],

    "corresponding_points": [
        {
            "id": "1",
            "position": [50.971296, 11.037630]
        },
        {
            "id": "2",
            "position": [50.971173, 11.037914]

        },
        {
            "id": "3",
            "position": [50.971456, 11.037915]

        },
        {
            "id": "4",
            "position": [50.971705, 11.037711]

        },
        {
            "id": "5",
            "position": [50.971402, 11.037796]

        },
        {
            "id": "5",
            "position": [50.971636, 11.037486]

        }
    ],

    "data": "boxes.txt",

    "classmap": {
        "1": "person",
        "2": "car"

    }
};

dataset["mapprovider"] = [
    {
        "name": "OpenStreetMap",
        "tilefunc": function (d) {
            return "http://" + "abc"[getRandomInt(0, 2)] + ".tile.openstreetmap.org/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "GoogleMaps satellite",
        "tilefunc": function (d) {
            return "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "s" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];
        }
    },
    {
        "name": "GoogleMaps Roadmap",
        "tilefunc": function (d) {
            return "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "m" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];
        }
    },
    {
        "name": "GoogleMaps Hybrid",
        "tilefunc": function (d) {
            return "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "y" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];
        }
    },
    {
        "name": "local OpenStreetMap",
        "tilefunc": function (d) {
            return "tiles/" + "osm_mapnik" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "local GoogleMaps satellite",
        "tilefunc": function (d) {
            return "tiles/" + "gmaps_satellite" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "local GoogleMaps Roadmap",
        "tilefunc": function (d) {
            return "tiles/" + "gmaps_roadmap" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "local GoogleMaps Hybrid",
        "tilefunc": function (d) {
            return "tiles/" + "gmaps_hybrid" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
];

var pi = Math.PI,
    tau = 2 * pi;

var svg = d3.select("svg"),
    width = +svg.attr("width"),
    height = +svg.attr("height")

    layer_map = svg.append("g");

    layer_hex = svg.append("g");

    foreignObject = svg.append("foreignObject")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", width)
        .attr("height", height);
    foBody = foreignObject.append("xhtml:body")
        .style("margin", "0px")
        .style("padding", "0px")
        .style("background", "none")
        .style("width", width + "px")
        .style("height", height + "px");
    canvas = foBody.append("canvas")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", width)
        .attr("height", height);
    layer_sca = canvas.node().getContext("2d");

    layer_sym = svg.append("g");

var projection = d3.geoMercator()
    .scale((1 << 8 + 19) / tau)
    .translate([width / 2, height / 2])
    .center([11.037630, 50.971296]);

var color = d3.scaleSequential(d3.interpolateLab("white", "steelblue"))
    .domain([0, 70]);

var alpha = d3.scaleLinear()
    .domain([0, 70])
    .range([0.2, 1.0]);

var hexbin = d3.hexbin()
    .radius(5)
    .extent([[0, 0], [width, height]]);

buildUI(dataset);

d3.text(dataset["data"], function(error, raw) {
    if (error) throw error;

    var boxes = d3.dsvFormat("|").parseRows(raw);
    boxes.forEach((box, index) => {
        var coord = projection([box[5], box[4]]);
        box[5] = coord[0];
        box[4] = coord[1];
        return boxes[index] = box; // lonlat!
    });

    buildGraph(dataset["cameras"], dataset["corresponding_points"], boxes, null);
});

$("li").click(function () {
    $(this).toggleClass("option-selected");

    if ($(this).data("type") === "mapprovider") {

        console.log($(this).data("type"));
        // $("#mapprovider-selector .li").each(function() {
        //     $(this).removeClass("option-selected");
        // });
        $("#mapprovider-selector li").removeClass("option-selected");
        $(this).addClass("option-selected");
    }

    refresh();
});

function refresh() {
    //
    // buildGraph()
}

function buildUI(dataset) {

    console.log(dataset["cameras"]);

    var cameras = d3.select("#camera-selector")
        .append("ul")
        .attr("class", "list-group mb-3");

    cameras.selectAll("ul")
        .data(dataset["cameras"])
        .enter()
            .append("li")
            .attr("class", "list-group-item d-flex justify-content-between lh-condensed option-selected")
            .attr("data-type", "camera")
            .attr("data-id", function (d) {
                return d["id"];
            })
        .append("h6")
            .attr("class", "my-0")
            .text(function (d) {
                return d["id"] + " : " + d["name"];
        });

    var mapProvider = d3.select("#mapprovider-selector")
        .append("ul")
        .attr("class", "list-group mb-3");

    mapProvider.selectAll("ul")
        .data(dataset["mapprovider"])
        .enter()
        .append("li")
        .attr("data-type", "mapprovider")
        .attr("class", function(d, i) {
            var classes = "list-group-item d-flex justify-content-between lh-condensed";

            if (i === 0) {
                classes += " option-selected"
            }

            return classes;
        })
        .append("h6")
        .attr("class", "my-0")
        .text(function (d) {
            return d["name"];
        });

    console.log(mapProvider);

// <ul class="list-group mb-3">
//         <li class="list-group-item d-flex justify-content-between lh-condensed">
//         <div>
//         <h6 class="my-0">Camera 1 : MotoZ</h6>
//     </div>
//     </li>
//     </ul>

}

function buildGraph(cameras, points, boxes, classmap) {

    // data:
    // timestamp device class confidence lat lon minx miny maxx maxy

    console.log(boxes[0])

    var boxesRaw = [];
    boxes.forEach((box, index) => {
        boxesRaw.push([box[5], box[4]]);
    });

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

    // symbols

    var symbols = [];
    cameras.forEach((cam, index) => {
        cam["type"] = "CAMERA";
        symbols.push(cam);
    });
    points.forEach((point, index) => {
        point["type"] = "POINT";
        symbols.push(point);
    });

    var symbols = layer_sym
        .attr("class", "layer_sym")
        .selectAll(".symbol")
        .data(symbols)
        .enter()
        .append("g")
        .attr("transform", function(d) {
            var coords = projection([d["position"][1], d["position"][0]]);
            return "translate(" + coords[0] + "," + coords[1] + ")";
        });

    symbols.append("circle")
        .attr("class", "sym")
        .attr("r", 10)
        .attr("cx", 0)
        .attr("cy", 0)
        .attr("fill-opacity", "1")
        .style("fill", function(d) {
            if (d["type"] === "CAMERA") {
                return "red";
            }
            if (d["type"] === "POINT") {
                return "grey";
            }

            return "white";
        });

    symbols.append("text")
        .attr("height", 10)
        .attr("width", 10)
        .attr("transform", "translate(" + -5.5 + "," + +5 + ")")
        .text(function(d) { return d["type"][0] });
}

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