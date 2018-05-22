function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function calculateTicksForTimespan(extent, numberOfTicks) {

    if (numberOfTicks <= 2) {
        return extent;
    }

    // slice timespan in x equivalent parts
    // for each part find best breakpoint (day 00:00 / 12:00 , hour :00 / :30 / :15 / :45 , minute :*0 , :*5 , :**
}