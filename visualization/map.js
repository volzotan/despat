
var mapproviders = [
    {
        "name": "OpenStreetMap",
        "tilefunc": function (d) {
            return "http://" + "abc"[getRandomInt(0, 2)] + ".tile.openstreetmap.org/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        },
        "tilefunc_local": function (d) {
            return "tiles/" + "osm_mapnik" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "GoogleMaps satellite",
        "tilefunc": function (d) {
            return "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "s" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];
        },
        "tilefunc_local": function (d) {
            return "tiles/" + "gmaps_satellite" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "GoogleMaps Roadmap",
        "tilefunc": function (d) {
            return "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "m" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];
        },
        "tilefunc_local": function (d) {
            return "tiles/" + "gmaps_roadmap" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    },
    {
        "name": "GoogleMaps Hybrid",
        "tilefunc": function (d) {
            return "http://mt" + getRandomInt(0, 2) + ".google.com/vt/lyrs=" + "y" + "@132&hl=de&x=" + d[0] + "&y=" + d[1] + "&z=" + d[2];
        },
        "tilefunc_local": function (d) {
            return "tiles/" + "gmaps_hybrid" + "/" + d[2] + "/" + d[0] + "/" + d[1] + ".png";
        }
    }
];

var initialMapProvider = 0;

var parseDate = d3.timeParse("%Y-%m-%d %H:%M:%S.%f"),
    parseDateFallback = d3.timeParse("%Y-%m-%d %H:%M:%S");

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

var layer_hbg = view.append("g"),
    layer_hex = view.append("g"),
    layer_sca = null,
    layer_sym = view.append("g");

var layer_leg = svg.append("g")
    .attr("class", "layer_leg");

var svgTime = d3.select("#timeselector svg")
    .attr("width", width)
    .attr("height", 50);

var timeBar = svgTime.append("g");

var center = [50.971296+0.0001, 11.037630+0.0002, ];

var projection,
    tiles,
    zoom;

function zoomed() {
    var transform = d3.event.transform;
    view.attr("transform", transform);

    // TODO
    // dont use canvas in basewidth size
    // build "viewport" canvas in width size and translate
    // if (layer_sca != null) {
    //     console.log("redraw");
    //     layer_sca.draw();
    // }

    // layer_sym
    //     .attr("transform", transform)
    //     // .style("stroke-width", 1 / transform.k);
}

var boxes = null,
    boxesFiltered = null,
    dataset = null;

    var settingHexSize = 5;

    var settingFilterConfidence = [0.25, 1.0],
        settingFilterTime       = null,
        settingFilterSession    = null,
        settingFilterClass      = null,
        settingFilterAction     = null;

window.onerror = function(message, source, lineno, colno, error) {
    $("#overlay").hide();
    $("#overlayError").show();
    $("#overlayError .errorMessage").text("Error: " + message);
};

if (window.location.hash) {

    dataset_path = "data/" + window.location.hash.substring(1) + ".json";
    retrieveDatasetFileViaRequest(dataset_path);

} else {

    document.getElementById('files').addEventListener('change', handleFileSelect, false);

    $("#overlay").hide();
    $("#overlayLoad").show();

    // throw "no dataset specified";
}

function retrieveDatasetFileViaRequest(dataset_path) { 
    d3.json(dataset_path, function(input) {

        if (input === null) {
            throw("input dataset missing");
        }

        d3.text("data/" + input["data"], function(error, raw) {
            if (error) throw error;

            start(input, raw);
        });
    });
}

function handleFileSelect(evt) {

    $("#overlay").show();
    $("#overlayLoad").hide();

    var files = evt.target.files; // FileList object

    for (var i = 0, f; f = files[i]; i++) {

        console.log("---- Load file ----");
        console.log("name: " + f.name);
        console.log("type: " + f.type);
        console.log("size: " + f.size);

        if (f.lastModifiedDate != null) {
            console.log("lmod: " + f.lastModifiedDate.toLocaleDateString());
        }

        if (f.type != "application/zip") {
            throw new Error("not a ZIP file");
        }

        var zip = new JSZip();
        zip.loadAsync(f).then(function(zip) {
            
            // check if all required files are present:
            var dataset_file = zip.file("info.json");

            if (dataset_file === null) {
                throw new Error("File info.json is missing in the ZIP archive");
            }

            dataset_file.async("string").then(function(result) {
                var dataset = JSON.parse(result);
                console.log(dataset["data"]);

                var box_file = zip.file(dataset["data"])

                if (box_file === null) {
                    throw new Error("File " + dataset["data"] + " is missing in the ZIP archive");
                }

                box_file.async("string").then(function(result) {
                    start(dataset, result);
                });    
            });
        });
    }
}

function start(dataset_json, boxes_text) {

    $("#overlayLoad").hide();

    dataset = dataset_json;
    dataset["mapprovider"] = mapproviders;

    var center = dataset["sessions"][0]["position"];                                                                  // TODO
    initProjection(center);

    drawLayerMap(mapproviders[initialMapProvider]["tilefunc"]);

    boxes = d3.dsvFormat("|").parse(boxes_text);

    boxes.forEach((box, index) => {
        // var coord = projection([box.lon, box.lat]), // lonlat!
        item = Array(11);

        var coords = projection([box["lon"], box["lat"]]);

        item[0] = parseDate(box["timestamp"]);
        item[1] = box["device"];
        item[2] = box["class"];
        item[3] = box["confidence"];
        item[4] = coords[1];
        item[5] = coords[0];
        item[6] = box["minx"];
        item[7] = box["miny"];
        item[8] = box["maxx"];
        item[9] = box["maxy"];
        item[10] = +box["action"];

        if (item[0] === null) {
            item[0] = parseDateFallback(box["timestamp"]);
        }

        boxes[index] = item;
    });

    // boxes = boxes.slice(0, 100);

    buildGraph(dataset["datasets"], dataset["corresponding_points"], null);
    buildUI(dataset);

    $("#overlay").hide();

}

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
        $("svg .legend_layer_" + layername).toggle("invisible");

        if (layername === "sca") {
            $("svg .layer_sca foreignObject").toggle("invisible");
        }

        return;
    }

    if ($(this).data("type") === "session") {
        var sessionIds = [];
        d3.selectAll("#session-selector li.option-selected").each(function(d, i) {
            sessionIds.push(d3.select(this).attr("data-id"));
        });

        settingFilterSession = sessionIds;
        filter();

        return;
    }

    if ($(this).data("type") === "class") {
        var classIds = [];
        d3.selectAll("#classes-selector li.option-selected").each(function(d, i) {
            classIds.push(d3.select(this).attr("data-id"));
        });

        settingFilterClass = classIds;
        filter();

        return;
    }

    if ($(this).data("type") === "mapprovider") {
        $("#mapprovider-selector li").removeClass("option-selected");
        $(this).addClass("option-selected");

        if ($("#toggleMapCached").hasClass("option-selected")) {
            drawLayerMap(mapproviders[+$(this).data("id")]["tilefunc_local"]);
        } else {
            drawLayerMap(mapproviders[+$(this).data("id")]["tilefunc"]);
        }

        return;
    }

    if ($(this).attr("id") === "toggleMapCached") {
        var e = $("#mapprovider-selector li.option-selected");

        if ($(this).hasClass("option-selected")) {
            drawLayerMap(mapproviders[+e.data("id")]["tilefunc_local"]);
        } else {
            drawLayerMap(mapproviders[+e.data("id")]["tilefunc"]);
        }

        return;
    }

    if ($(this).attr("id") === "toggleActions") {
        if ($(this).hasClass("option-selected")) {
            settingFilterAction = [0, 1];
            filter();
        } else {
            settingFilterAction = null;
            filter();
        }

        return;
    }

};

function initProjection(center) {
    projection = d3.geoMercator()
        .scale((1 << 8 + 19) / tau)
        .translate([basewidth / 2, baseheight / 2])
    // .translate([width / 2, height / 2])
        .center([center[1], center[0]]);
    // .center([11.037630-0.001, 50.971296-0.0005]);

    tiles = d3.tile()
        .size([basewidth, baseheight])
        .scale(projection.scale() * tau)
        .translate(projection([0, 0]))
        ();

    zoom = d3.zoom()
        .scaleExtent([0.4, 7])
        .translateExtent([[0, 0], [basewidth, baseheight]])
        .on("zoom", zoomed);

    svg.call(zoom)
        .call(zoom.transform, d3.zoomIdentity
            .translate(-(basewidth-width)/2, -(baseheight-height)/2));
}

function buildUI(dataset) {

    var sessions = d3.select("#session-selector")
        .append("ul")
        .attr("class", "list-group mb-3");

    sessions.selectAll("ul")
        .data(dataset["sessions"])
        .enter()
            .append("li")
            .attr("class", "list-group-item d-flex justify-content-between lh-condensed selectable option-selected")
            .attr("data-type", "session")
            .attr("data-id", function (d) {
                return d["id"];
            })
        .append("h6")
            .attr("class", "my-0")
            .text(function (d) {
                return d["id"] + " : " + d["sessionname"];
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
    $("#toggleMapCached").click(clickFunction);
    $("#toggleActions").click(clickFunction);

    // $("#sliderHex").on("change", function(event) {
    //     console.log(event);
    // });

    $("#sliderHexAlpha").on("input", function(event) {
        d3.select(".layer_hex").attr("opacity", $(this).val()/100);
    });

    $("#sliderHexSize").on("input", function(event) {
        settingHexSize = $(this).val();
        filter();
    });

    $("#sliderMapAlpha").on("input", function(event) {
        d3.select(".layer_map").attr("opacity", $(this).val()/100);
    });

    // set defaults

    // $("#toggleMapCached").click();

    $("li[data-type=layer][data-id=hbg]").click();
    $("li[data-type=layer][data-id=sca]").click();
    $("li[data-type=layer][data-id=sym]").click();

    $("#sliderHexAlpha").val( 90).trigger("input");
    $("#sliderHexSize ").val(settingHexSize);
    $("#sliderMapAlpha").val(100).trigger("input");
}

function buildGraph(sessions, points, classmap) {

    drawLayerHbg();

    filter();

    drawLayerSca();

    drawLayerSym();

    draw_timeBar("#svg-time");
}

function filter() {

    boxesFiltered = boxes;
    boxesFiltered = boxesFiltered.filter(function (d) {
        if (settingFilterConfidence != null) {
            if (d[3] < settingFilterConfidence[0]) {
                return false
            }

            if (d[3] > settingFilterConfidence[1]) {
                return false
            }
        }

        if (settingFilterTime != null) {
            if (d[0] < settingFilterTime[0]) {
                return false
            }

            if (d[0] > settingFilterTime[1]) {
                return false;
            }
        }

        if (settingFilterSession != null) {
            if (!settingFilterSession.includes(d[1])) {
                return false;
            }
        }

        if (settingFilterClass != null) {
            if (!settingFilterClass.includes(d[2])) {
                return false;
            }
        }

        return true;
    });

    drawLayerHex(settingHexSize);
    draw_confidence_frequency("#svg-confidence", boxesFiltered);
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
        // .attr("filter", "url(#grayscale)")
        .attr("x", function(d) { return ((d[0] + tiles.translate[0]) * tiles.scale)|0; }) // double to int
        .attr("y", function(d) { return ((d[1] + tiles.translate[1]) * tiles.scale)|0; })
        .attr("width", tiles.scale)
        .attr("height", tiles.scale);
}

function drawLayerHex(octagonRadius) {

    $(".layer_hex").empty();
    $(".legend_layer_hex").empty();

    var hexbin = d3.hexbin()
        .x(function (d) {
            return d[5];
        })
        .y(function (d) {
            return d[4];
        })
        .radius(octagonRadius)
        .extent([[0, 0], [width, height]]);

    var hbins = hexbin(boxesFiltered);

    hbins.sort(function (a, b) {
        return a.length - b.length;
    });

    var percentage_cutoff = (hbins.length / 100) * 1,
        hbins_minmaxcutoff = d3.extent(
            hbins.slice(percentage_cutoff, hbins.length - percentage_cutoff),
            function (d) {
                return d.length;
            }
        );

    var scaleViridis = d3.scaleSequential(d3.interpolateViridis).domain(hbins_minmaxcutoff),
        scaleBlue = d3.scaleSequential(d3.interpolateLab("white", "blue")).domain(hbins_minmaxcutoff),
        scaleYellow = d3.scaleSequential(d3.interpolateLab("white", "yellow")).domain(hbins_minmaxcutoff);

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
        .attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
        })
        .attr("fill", function (d) {
            if (settingFilterAction != null) {
                var ratio = calculateActionRatio(d);
                return calculateRatioColor([scaleYellow(ratio[0]), scaleBlue(ratio[1])]);
            } else {
                return scaleViridis(d.length);
            }
        });

    // legend
    drawLegend(hbins_minmaxcutoff);
}

function drawLegend(extent) {
    var legendHeight = 200,
        legendWidth = 20;

    var gradient = layer_leg.append('defs')
        .append('linearGradient')
        .attr('id', 'gradient')
        .attr('x1', '0%') // bottom
        .attr('y1', '100%')
        .attr('x2', '0%') // to top
        .attr('y2', '0%')
        .attr('spreadMethod', 'pad');

    var colorLeg = d3.scaleSequential(d3.interpolateViridis)
        .domain([0, 100-1]);

    var colorPct = Array(100);
    for (i = 0; i < colorPct.length; i++) {
        colorPct[i] = [i + "%", colorLeg(i)];
    }

    colorPct.forEach(function(d) {
        gradient.append('stop')
            .attr('offset', d[0])
            .attr('stop-color', d[1])
            .attr('stop-opacity', 1);
    });

    var legend_hex = layer_leg.append("g")
        .attr("class", "legend_layer_hex")
        .attr("transform", function (d) {
            return "translate(" + (svg.attr("width")-legendWidth-15) + "," + (svg.attr("height")-legendHeight-15) + ")";
        });

    legend_hex.append('rect')
        .attr('x1', 0)
        .attr('y1', 0)
        .attr('width', legendWidth)
        .attr('height', legendHeight)
        .style('fill', 'url(#gradient)');

    var legendScale = d3.scaleLinear()
        .domain([-3, 3])
        .domain(extent)
        .range([legendHeight, 0]);

    var legendAxis = d3.axisLeft()
        .scale(legendScale)
        // .tickValues(d3.range(0, 105, 10))
        // .ticks(5)
        .tickFormat(d3.format("d"));

    legend_hex.append("g")
        .attr("class", "legend axis")
        .attr("transform", "translate(" + -3 + ", 0)")
        .call(legendAxis);
}

function drawLayerSca() {

    layer_sca = view.append("g")
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

    layer_sca.draw = function() {
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

    layer_sca.draw();
}

function drawLayerSym() {

    $(".layer_sym").empty();

    var symbols = [];
    dataset["sessions"].forEach((session, index) => {
        var newSymbol = {};
        newSymbol["type"] = "CAMERA";
        newSymbol["position"] = session["position"];
        symbols.push(newSymbol);

        session["corresponding_points"].forEach((point, index) => {
            var newSymbol = {};
            newSymbol["type"] = "POINT";
            newSymbol["position"] = point["position"];
            symbols.push(newSymbol);
        });
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

function draw_timeBar(classname, redraw) {

    if (boxes.length < 1) {
        console.log("no detections.");
        return;
    }

    var timeMinMax = d3.extent(boxes, function(d) { return d[0]; }),
        binNumber = 200,
        binSize = (timeMinMax[1] - timeMinMax[0]) / binNumber,
        timeBins = new Array(binNumber).fill(0);

    boxes.forEach((box, index) => {
        var timestamp = box[0],
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

    var svg = d3.select(classname)
        .attr("height", 60);

    var margin = {"top": 5, "right": 5, "bottom": 12, "left": 5},
        width = +svg.attr("width")-margin.left-margin.right,
        height = +svg.attr("height")-margin.top-margin.bottom;

    var x = d3.scaleBand()
        // .domain([0, timeBins.length])
        .domain(timeBins.map(function(d, i) { return i; }))
        .range([0, width])
        .padding(0.1);

    var y = d3.scaleLinear()
        // .domain(d3.extent(timeBins))
        .domain([0, d3.max(timeBins)])
        .rangeRound([height, 0]);

    var timeScale = d3.scaleTime()
        .domain(timeMinMax)
        .range([1, width - 2 * 1]);

    var tickValues = calculateTicksForTimespan(timeMinMax, 4);

    var axis = d3.axisBottom(timeScale)
        // .tickFormat(d3.timeFormat("%Y-%m-%d"))
        .tickFormat(d3.timeFormat("%d.%m %H:%M"))
        .tickValues(tickValues);
        // .ticks(5);

    svg.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(" + margin.left + "," + (height+margin.top) + ")")
        .call(axis)
        .selectAll("text")
        .attr("dx", function(d, i) {
            if (i === 0) {
                return ".8em";
            }

            return "-.8em";
        })
        .attr("dy", ".15em")
        .style("text-anchor", function(d, i) {
            if (i === 0) {
                return "start";
            }

            return "end";
        });

    svg.append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
        .selectAll(".bar")
        .data(timeBins)
        .enter().append("rect")
        .attr("class", function(d) {
            return "bar";
        })
        .attr("x", function(d, i) { return x(i); })
        .attr("y", function(d) { return y(d); })
        .attr("width", x.bandwidth())
        .attr("height", function(d) { return height - y(d); });

    // brush

    var brush = d3.brushX()
        .extent([[margin.left, margin.top], [width+margin.left, height+margin.top]])
        // .on("start brush end", function() {
        // .on("end", function() {
        .on("brush", function() {
        // .on("brush", function() {
            var s = d3.event.selection;
            if (s == null) {

            } else {
                var sx = s.map(timeScale.invert);
                settingFilterTime = sx;
                filter();
            }
        });

    var gBrush = svg.append("g")
        .attr("class", "brush")
        .call(brush);

    gBrush.call(brush.move, [margin.left, width+margin.left]);

    // var handle = gBrush.selectAll(".handle--custom")
    //     .data([{type: "w"}, {type: "e"}])
    //     .enter().append("path")
    //     .attr("class", "handle--custom")
    //     .attr("fill", "#666")
    //     .attr("fill-opacity", 0.8)
    //     .attr("stroke", "#000")
    //     .attr("stroke-width", 1.5)
    //     .attr("cursor", "ew-resize")
    //     .attr("d", d3.arc()
    //         .innerRadius(0)
    //         .outerRadius(height / 2)
    //         .startAngle(0)
    //         .endAngle(function(d, i) { return i ? Math.PI : -Math.PI; }));
}


function drawClassSelector() {

    var classCount = Array(dataset["classmap"].length).fill(0);

    boxes.forEach((box, index) => {
        classCount[box[2]] += 1;
    });

    var classes = d3.select("#classes-selector")
        .append("ul")
        .attr("class", "list-group mb-3")
        .selectAll("ul")
        .data(dataset["classmap"])
        .enter()
        .append("li")
        .attr("class", function(d, i) {
            var classes = "list-group-item d-flex justify-content-between lh-condensed selectable";
            if (classCount[i] === 0) {
                return classes;
            }

            classes += " option-selected";
            return classes;

        })
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
        margin = {"top": 5, "right": 10, "bottom": 5, "left": 28};
        width = +svg.attr("width")-margin.left-margin.right,
        height = +svg.attr("height")-margin.top-margin.bottom,
        g = svg.append("g");

    var x = d3.scaleLinear()
        .domain([0, bins.length])
        .rangeRound([0, width]);

    var y = d3.scaleLinear()
        .domain(d3.extent(bins, function (d) { return d.length; }))
        .rangeRound([height, 0]);

    var axis = d3.axisLeft()
        .scale(y)
        .ticks(3);

    g.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(" + (margin.left-3) + ", " + margin.top + ")")
        .call(axis);

    var line = d3.line()
        .x(function(d, i) { return x(i); })
        .y(function(d) { return y(d.length); });

    g.append("path")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
        .datum(bins)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-linejoin", "round")
        .attr("stroke-linecap", "round")
        .attr("stroke-width", 1.5)
        .attr("d", line);
}

function draw_confidence_frequency(classname, data) {

    $(classname).empty();

    var confidences = Array(data.length);

    data.forEach((box, index) => {
        confidences[index] = box[3];
    });

    confidences.sort();

    var svg = d3.select(classname),
        margin = {"top": 5, "right": 10, "bottom": 5, "left": 28},
        width = +svg.attr("width")-margin.left-margin.right,
        height = +svg.attr("height")-margin.top-margin.bottom,
        g = svg.append("g");

    var x = d3.scaleLinear()
        .domain([0, confidences.length])
        .rangeRound([0, width]);

    var y = d3.scaleLinear()
        .domain([0, 1.0])
        .rangeRound([height, 0]);

    var axis = d3.axisLeft()
        .scale(y)
        .ticks(3);

    g.append("g")
        .attr("class", "axis")
        .attr("transform", "translate(" + (margin.left-3) + ", " + margin.top + ")")
        .call(axis);

    var line = d3.line()
        .x(function(d, i) { return x(i); })
        .y(function(d) { return y(d); });

    g.append("path")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
        .datum(confidences)
        .attr("fill", "none")
        .attr("stroke", "steelblue")
        .attr("stroke-linejoin", "round")
        .attr("stroke-linecap", "round")
        .attr("stroke-width", 1.5)
        .attr("d", line);

    // brush

    // var brush = d3.brushX()
    //     .extent([[margin.left, margin.top], [width+margin.left, height+margin.top]])
    //     .on("start brush end", brushmoved);
    //
    // var gBrush = g.append("g")
    //     .attr("class", "brush")
    //     .call(brush);
    //
    // gBrush.selectAll("rect").attr("height", height);
    // gBrush.selectAll(".resize").append("path").attr("d", resizePath);

    // gBrush.call(brush.move, [margin.left, width+margin.left]);
}

// function resizePath(d) {
//
//     console.log("foo");
//
//     var e = +(d == "e"),
//         x = e ? 1 : -1,
//         y = height / 3;
//     return "M" + (.5 * x) + "," + y
//         + "A6,6 0 0 " + e + " " + (6.5 * x) + "," + (y + 6)
//         + "V" + (2 * y - 6)
//         + "A6,6 0 0 " + e + " " + (.5 * x) + "," + (2 * y)
//         + "Z"
//         + "M" + (2.5 * x) + "," + (y + 8)
//         + "V" + (2 * y - 8)
//         + "M" + (4.5 * x) + "," + (y + 8)
//         + "V" + (2 * y - 8);
// }