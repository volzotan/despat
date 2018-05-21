
var mapproviders = [
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

var initialMapProvider = 4; // TODO

var pi = Math.PI,
    tau = 2 * pi;

var width = Math.min(1140, window.innerWidth),
    height = 600;

var svg = d3.select("#map svg")
    .attr("width", width)
    .attr("height", height);

var basewidth = width*3,
    baseheight = height*4;

var view = svg.append("g");

var layer_map = view.append("g")
        .attr("class", "layer_map");

var layer_hbg = view.append("g");

var layer_hex = view.append("g");

var layer_sca = null;

var layer_sym = view.append("g");


var svgTime = d3.select("#timeselector svg")
    .attr("width", width)
    .attr("height", 50);

var timeBar = svgTime.append("g");


var center = [11.037630, 50.971296];

var projection = d3.geoMercator()
    .scale((1 << 8 + 19) / tau)
    .translate([basewidth / 2, baseheight / 2])
    // .translate([width / 2, height / 2])
    .center(center);
    // .center([11.037630-0.001, 50.971296-0.0005]);

var tiles = d3.tile()
    .size([basewidth, baseheight])
    .scale(projection.scale() * tau)
    .translate(projection([0, 0]))
    ();

var zoom = d3.zoom()
    .scaleExtent([0.4, 7])
    .translateExtent([[0, 0], [basewidth, baseheight]])
    .on("zoom", zoomed);

svg.call(zoom)
    .call(zoom.transform, d3.zoomIdentity
        .translate(-(basewidth-width)/2, -(baseheight-height)/2));

function zoomed() {

    var transform = d3.event.transform;

    view.attr("transform", transform);

    // layer_hex.attr("transform", transform);
    //
    // layer_sym
    //     .attr("transform", transform)
    //     // .style("stroke-width", 1 / transform.k);
    //
    // layer_map.attr("transform", transform);
}

function stringify(scale, translate) {
    var k = scale / 256, r = scale % 1 ? Number : Math.round;
    return "translate(" + r(translate[0] * scale) + "," + r(translate[1] * scale) + ") scale(" + k + ")";
}

var boxes = null;
    dataset = null;

d3.json("dataset.json", function(input) {

    input["mapprovider"] = mapproviders;
    dataset = input;

    drawLayerMap(mapproviders[initialMapProvider]["tilefunc"]);

    d3.text(dataset["data"], function(error, raw) {
        if (error) throw error;

        boxes = d3.dsvFormat("|").parse(raw);
        boxes.forEach((box, index) => {
            // var coord = projection([box.lon, box.lat]), // lonlat!
                item = Array(10);

            var coords = projection([box["lon"], box["lat"]]);

            item[0] = box["timestamp"];
            item[1] = box["device"];
            item[2] = box["class"];
            item[3] = box["confidence"];
            item[4] = coords[1];
            item[5] = coords[0];
            item[6] = box["minx"];
            item[7] = box["miny"];
            item[8] = box["maxx"];
            item[9] = box["maxy"];

            return boxes[index] = item;
        });

        buildGraph(dataset["cameras"], dataset["corresponding_points"], null);
        buildUI(dataset);

        $("#overlay").hide();
    });
});

var clickFunction = function () {

    $(this).toggleClass("option-selected");

    // if ($(this).data("type") === "layer" && $(this).data("id") === "sca") {
    //     if ($(this).hasClass("option-selected") === true) {
    //         drawLayerSca();
    //     } else {
    //         $("#layer_sca").empty();
    //     }
    // }

    if ($(this).data("type") === "layer") {
        var layername = $(this).data("id");
        $("svg .layer_" + layername).toggle("invisible");

        return;
    }

    if ($(this).data("type") === "mapprovider") {
        $("#mapprovider-selector li").removeClass("option-selected");
        $(this).addClass("option-selected");

        drawLayerMap(mapproviders[+$(this).data("id")]["tilefunc"]);

        return;
    }

};

function buildUI(dataset) {

    var cameras = d3.select("#camera-selector")
        .append("ul")
        .attr("class", "list-group mb-3");

    cameras.selectAll("ul")
        .data(dataset["cameras"])
        .enter()
            .append("li")
            .attr("class", "list-group-item d-flex justify-content-between lh-condensed selectable option-selected")
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
        .attr("data-id", function(d, i) { return i; })
        .attr("class", function(d, i) {
            var classes = "list-group-item d-flex justify-content-between selectable lh-condensed";

            if (i === initialMapProvider) {
                classes += " option-selected"
            }

            return classes;
        })
        .append("h6")
        .attr("class", "my-0")
        .text(function (d) {
            return d["name"];
        });

    drawClassSelector();

    $("li.selectable").click(clickFunction);

    // $("#sliderHex").on("change", function(event) {
    //     console.log(event);
    // });

    $("#sliderHexAlpha").on("input", function(event) {
        d3.select(".layer_hex").attr("opacity", $(this).val()/100);
    });

    $("#sliderHexSize").on("input", function(event) {
        drawLayerHex($(this).val());
    });

    $("#sliderMapAlpha").on("input", function(event) {
        d3.select(".layer_map").attr("opacity", $(this).val()/100);
    });

    // set defaults

    $("li[data-type=layer][data-id=hbg]").click();
    $("li[data-type=layer][data-id=sca]").click();
    $("li[data-type=layer][data-id=sym]").click();

    $("#sliderHexAlpha").val( 85).trigger("input");
    $("#sliderHexSize ").val( 5).trigger("input");
    $("#sliderMapAlpha").val(100).trigger("input");
}

function buildGraph(cameras, points, classmap) {

    drawLayerHbg();

    // drawLayerHex(30);
    draw_confidence_frequency("#svg-confidence", boxes);

    drawLayerSca();

    drawLayerSym();

    draw_timeBar("#svg-time");

}

function drawLayerHbg() {

    layer_hbg
        .attr("class", "layer_hbg")
        .append("rect")
        .attr("width", basewidth)
        .attr("height", baseheight)
        .attr("fill", function() { return d3.interpolateViridis(0); });

}

function drawLayerMap(tileFunc) {

    $(".layer_map").empty();

    layer_map
        .attr("class", "layer_map")
        .selectAll("image")
        .data(tiles)
        .enter().append("image")
        .attr("xlink:href", tileFunc)
        .attr("filter", "url(#grayscale)")
        .attr("x", function(d) { return ((d[0] + tiles.translate[0]) * tiles.scale)|0; }) // double to int
        .attr("y", function(d) { return ((d[1] + tiles.translate[1]) * tiles.scale)|0; })
        .attr("width", tiles.scale)
        .attr("height", tiles.scale);
}

function drawLayerHex(octagonRadius) {

    $(".layer_hex").empty();

    console.log(octagonRadius);
    console.log(boxes.length);

    var boxesRaw = [];
    boxes.forEach((box, index) => {
        boxesRaw.push([box[5], box[4]]);
    });

    var hexbin = d3.hexbin()
        .radius(octagonRadius)
        .extent([[0, 0], [width, height]]);

    var hbins = hexbin(boxesRaw);

    console.log(hbins.length);

    hbins.sort(function(a, b){
        return a.length - b.length;
    });

    var percentage_cutoff = (hbins.length/100) * 1,
        hbins_minmaxcutoff = hbins.slice(percentage_cutoff, hbins.length-percentage_cutoff);

    var color = d3.scaleSequential(d3.interpolateViridis)
        .domain(d3.extent(hbins_minmaxcutoff, function(d) { return d.length; }));

    // heatmap graph
    draw_heatmap_bin_frequency("#svg-heatmap", hbins);

    layer_hex
        .attr("class", "layer_hex")
        .append("g")
        .attr("class", "hexagon")
        .attr("clip-path", "url(#clip)")
        .selectAll("path")
        .data(hbins)
        .enter().append("path")
        .attr("d", hexbin.hexagon())
        .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
        .attr("fill", function(d) { return color(d.length); });
}

function drawLayerSca() {

    var layer_sca = view.append("g")
            .attr("class", "layer_sca")
            .append("foreignObject")
            .attr("x", 0)
            .attr("y", 0)
            .attr("width", basewidth)
            .attr("height", baseheight),
        foBody = layer_sca.append("xhtml:body")
            .style("margin", "0px")
            .style("padding", "0px")
            .style("background", "none")
            .style("width", basewidth + "px")
            .style("height", baseheight + "px"),
        canvas = foBody.append("canvas")
            .attr("x", 0)
            .attr("y", 0)
            .attr("width", basewidth)
            .attr("height", baseheight),
        context = canvas.node().getContext("2d");

    var draw = function() {
        context.clearRect(0, 0, basewidth, baseheight);
        boxes.forEach((box, index) => {

            x = box[5];
            y = box[4];

            context.beginPath();
            context.fillStyle = "#333";
            context.strokeStyle = "#333";
            context.arc(x, y, 0.5, 0, 2 * Math.PI);
            // layer_sca.stroke();
            context.fill();
            context.closePath();
        });
    };

    draw();
}

function drawLayerSym() {

    $(".layer_sym").empty();

    var symbols = [];
    dataset["cameras"].forEach((cam, index) => {
        cam["type"] = "CAMERA";
        symbols.push(cam);
    });
    dataset["corresponding_points"].forEach((point, index) => {
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

function draw_timeBar(classname) {

    var	parseDate = d3.timeParse("%Y-%m-%d %H:%M:%S.%f"),
        timeMinMax = d3.extent(boxes, function(d) { return parseDate(d[0]); }),
        binNumber = 100,
        binSize = (timeMinMax[1] - timeMinMax[0]) / binNumber,
        timeBins = new Array(binNumber).fill(0);

    boxes.forEach((box, index) => {
        var timestamp = parseDate(box[0]),
            bin = Math.floor((timestamp - timeMinMax[0]) / binSize);

        if (timestamp === null) {
            console.log("warn: illegal data in boxes");
            return;
        }

        if (bin >= binNumber) {
            bin = binNumber-1;
        }

        timeBins[bin] += 1;
    });

    var svg = d3.select(classname),
        width = +svg.attr("width"),
        height = +svg.attr("height"),
        timeAxisHeight = 15,
        g = svg.append("g");

    var x = d3.scaleBand()
        // .domain([0, timeBins.length])
        .domain(timeBins.map(function(d, i) { return i; }))
        .range([0, width])
        .padding(0.1);

    var y = d3.scaleLinear()
        // .domain(d3.extent(timeBins))
        .domain([0, d3.max(timeBins)])
        .rangeRound([height-timeAxisHeight, 0]);

    var timeScale = d3.scaleTime()
        .domain(timeMinMax)
        .range([1, width - 2 * 1]);

    var axis = d3.axisBottom(timeScale)
        // .tickFormat(d3.timeFormat("%Y-%m-%d"))
        .tickFormat(d3.timeFormat("%d.%m %H:%M"))
        .ticks(5);

    var brush = d3.brushX()
        .extent([[0, 0], [width, height]])
        .on("start brush end", brushmoved);

    var gBrush = g.append("g")
        .attr("class", "brush")
        .call(brush);

    var handle = gBrush.selectAll(".handle--custom")
        .data([{type: "w"}, {type: "e"}])
        .enter().append("path")
        .attr("class", "handle--custom")
        .attr("fill", "#666")
        .attr("fill-opacity", 0.8)
        .attr("stroke", "#000")
        .attr("stroke-width", 1.5)
        .attr("cursor", "ew-resize")
        .attr("d", d3.arc()
            .innerRadius(0)
            .outerRadius(height / 2)
            .startAngle(0)
            .endAngle(function(d, i) { return i ? Math.PI : -Math.PI; }));

    svg.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(0," + (height - timeAxisHeight) + ")")
        .call(axis)
        .selectAll("text")
        .style("text-anchor", "end")
        .attr("dx", "-.8em")
        .attr("dy", ".15em");
        // .attr("transform", "rotate(-65)");

    g.selectAll(".bar")
        .data(timeBins)
        .enter().append("rect")
        .attr("class", "bar")
        .attr("x", function(d, i) { return x(i); })
        .attr("y", function(d) { return y(d); })
        .attr("width", x.bandwidth())
        .attr("height", function(d) { return height - timeAxisHeight - y(d); });
}

function brushmoved() {
    var s = d3.event.selection;
    if (s == null) {
        handle.attr("display", "none");
        circle.classed("active", false);
    } else {
        var sx = s.map(x.invert);
        circle.classed("active", function(d) { return sx[0] <= d && d <= sx[1]; });
        handle.attr("display", null).attr("transform", function(d, i) { return "translate(" + s[i] + "," + height / 2 + ")"; });
    }
}

function drawClassSelector() {

    var classCount = Array(dataset["classmap"].length).fill(0);

    boxes.forEach((box, index) => {
        classCount[box[2]-1] += 1;
    });

    var classes = d3.select("#classes-selector")
        .append("ul")
        .attr("class", "list-group mb-3")
        .selectAll("ul")
        .data(dataset["classmap"])
        .enter()
        .append("li")
        .attr("class", "list-group-item d-flex justify-content-between lh-condensed selectable")
        .attr("data-type", "class")
        .attr("data-id", function (d, i) {
            return i;
        })
        .attr("style", function(d, i) {
            if (classCount[i] === 0) {
                return "display: none!important;"
            }
            return "display: block;"

        });

    classes.append("h6")
        .attr("class", "my-0")
        .text(function (d) {
            return d;
        });
    classes.append("span")
        .attr("class", "text-muted")
        .text(function (d, i) {
            return classCount[i];
        });
}

function draw_heatmap_bin_frequency(classname, bins) {

    // expects already sorted bins array

    $(classname).empty();

    var svg = d3.select(classname),
        width = +svg.attr("width"),
        height = +svg.attr("height"),
        g = svg.append("g");

    var x = d3.scaleLinear()
        .domain([0, bins.length])
        .rangeRound([0, width]);

    var y = d3.scaleLinear()
        .domain(d3.extent(bins, function (d) { return d.length; }))
        .rangeRound([height, 0]);

    var line = d3.line()
        .x(function(d, i) { return x(i); })
        .y(function(d) { return y(d.length); });

    g.append("path")
        .datum(bins)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-linejoin", "round")
        .attr("stroke-linecap", "round")
        .attr("stroke-width", 1.5)
        .attr("d", line);
}

function draw_confidence_frequency(classname, boxes) {

    var confidences = Array(boxes.length);

    boxes.forEach((box, index) => {
        confidences[index] = box[3];
    });

    confidences.sort();

    var svg = d3.select(classname),
        width = +svg.attr("width"),
        height = +svg.attr("height"),
        g = svg.append("g");

    var x = d3.scaleLinear()
        .domain([0, confidences.length])
        .rangeRound([0, width]);

    var y = d3.scaleLinear()
        .domain([0, 1.1])
        .rangeRound([height, 0]);


    var axis = d3.axisBottom(y)
        .ticks(3);

    g.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(0," + 0 + ")")
        .call(axis)
        .selectAll("text")
        .style("text-anchor", "end")
        .attr("dx", "-.8em")
        .attr("dy", ".15em");
        //.attr("transform", "rotate(-90)");



    var line = d3.line()
        .x(function(d, i) { return x(i); })
        .y(function(d) { return y(d); });

    g.append("path")
        .datum(confidences)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-linejoin", "round")
        .attr("stroke-linecap", "round")
        .attr("stroke-width", 1.5)
        .attr("d", line);
}