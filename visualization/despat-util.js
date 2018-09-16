function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function findBreakpoint(start, end) {

    // for each section find best breakpoint (day 00:00 / 12:00 , hour :00 / :30 / :15 / :45 , minute :*0 , :*5 , :**)

    var a = new Date(start),
        b = new Date(end),
        diff = b-a,
        r = new Date(a.getTime() + diff/2),
        m = new Date(r);

    var preferredValuesHours    = [0, 12, 6, 18];
    var preferredValues         = [30, 15, 45, 5, 10, 20, 25, 35, 40, 50, 55];

    // hours

    for (var i=0; i < preferredValuesHours.length; i++) {
        m.setHours(preferredValuesHours[i]);
        m.setMinutes(0);
        m.setSeconds(0);
        m.setMilliseconds(0);
        if (m > a && m < b) {
            return m;
        }
        m = new Date(r);
    }

    for (var i=0; i<23; i+=1) {
        m.setHours(i);
        m.setMinutes(0);
        m.setSeconds(0);
        m.setMilliseconds(0);
        if (m > a && m < b) {
            return m;
        }
        m = new Date(r);
    }

    // minutes

    for (var i=0; i < preferredValues.length; i++) {
        m.setMinutes(preferredValues[i]);
        m.setSeconds(0);
        m.setMilliseconds(0);
        if (m > a && m < b) {
            return m;
        }
        m = new Date(r);
    }

    for (var i=0; i<59; i+=1) {
        m.setMinutes(i);
        m.setSeconds(0);
        m.setMilliseconds(0);
        if (m > a && m < b) {
            return m;
        }
        m = new Date(r);
    }

    // seconds

    for (var i=0; i < preferredValues.length; i++) {
        m.setSeconds(preferredValues[i]);
        m.setMilliseconds(0);
        if (m > a && m < b) {
            return m;
        }
        m = new Date(r);
    }

    for (var i=0; i<59; i+=1) {
        m.setSeconds(i);
        m.setMilliseconds(0);
        if (m > a && m < b) {
            return m;
        }
        m = new Date(r);
    }

    return m;
}

function calculateTicksForTimespan(extent, numberOfTicks) {

    var ticks = Array(numberOfTicks);

    ticks[0] = extent[0];
    ticks[numberOfTicks-1] = extent[1];

    var start = extent[0].getTime(),
        end = extent[1].getTime(),
        diff = end - start,
        section = diff / numberOfTicks-1;

    for (var i=1; i<numberOfTicks-1; i++) {
        ticks[i] = findBreakpoint(start + section * i, start + section * (i+1));
    }

    return ticks;
}

function calculateActionRatio(boxes) {
    filtered = boxes.filter(function (d) {
        if (d[10] == 1) {
            return false;
        }
        return true;
    });

    // [idle, active]
    return [boxes.length-filtered.length, filtered.length];
}

function calculateRatioColor(colors) {
    console.log(colors);
    var c1 = colors[0].substring(colors[0].indexOf("(")+1, colors[0].length-1).split(", "),
        c2 = colors[1].substring(colors[1].indexOf("(")+1, colors[1].length-1).split(", "),
        c3 = [];

    for (var i=0; i<3; i++) {
        c3[i] = Math.round(((+c1[i]) + (+c2[i]))/2);
    }

    return "rgb(" + c3[0] + ", " + c3[1] + ", " + c3[2] + ")";
}