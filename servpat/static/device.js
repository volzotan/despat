
function draw(classname, yAxisLabel, yBounds, arrayOffset) {

var data = []; 

var parseTime = d3.timeParse("%Y-%m-%d %H:%M:%S");

graph_status.forEach(function(entry) {
    var date = entry[3].substr(0, entry[3].length-7);
    data.push({"date": parseTime(date), "value": entry[arrayOffset]});
    console.log(date);
    console.log(parseTime(date));
});

var svg = d3.select(classname),
    margin = {top: 20, right: 20, bottom: 30, left: 50},
    width = +svg.attr("width") - margin.left - margin.right,
    height = +svg.attr("height") - margin.top - margin.bottom,
    g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

var x = d3.scaleTime()
    .rangeRound([0, width]);

var y = d3.scaleLinear()
    .rangeRound([height, 0]);

var line = d3.line()
    .x(function(d) { return x(d.date); })
    .y(function(d) { return y(d.value); });

  x.domain(d3.extent(data, function(d) { return d.date; }));

  if (yBounds === null) {
    y.domain(d3.extent(data, function(d) { return d.value; }));
  } else {
    if (yBounds[0] === null) {
        y.domain([d3.min(data, function(d) { return d.value; }, yBounds[1])]);
    } else if (yBounds[1] === null) {
        y.domain([yBounds[0], d3.max(data, function(d) { return d.value; })]);
    } else {
        y.domain(yBounds);
    }
  }

  g.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x))
    // .select(".domain")
    //   .remove();

  g.append("g")
      .call(d3.axisLeft(y))
    .append("text")
      .attr("fill", "#000")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", "0.71em")
      .attr("text-anchor", "end")
      .text(yAxisLabel);

  g.append("path")
      .datum(data)
      .attr("fill", "none")
      .attr("stroke", "steelblue")
      .attr("stroke-linejoin", "round")
      .attr("stroke-linecap", "round")
      .attr("stroke-width", 1.5)
      .attr("d", line);

}

draw(".graph-battery", "battery [%]", [0.0, 100.0], 8);
draw(".graph-memory", "free space [MB]", [0, null], 6);
