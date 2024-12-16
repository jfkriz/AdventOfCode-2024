# [Day 16: Reindeer Maze](https://adventofcode.com/2024/day/16)
## Part One

It's time again for the [Reindeer Olympics](https://adventofcode.com/2015/day/14)! This year, the big
event is the _Reindeer Maze_ , where the Reindeer compete for the _lowest
score_.

You and The Historians arrive to search for the Chief right as the event is
about to start. It wouldn't hurt to watch a little, right?

The Reindeer start on the Start Tile (marked `S`) facing _East_ and need to
reach the End Tile (marked `E`). They can move forward one tile at a time
(increasing their score by `1` point), but never into a wall (`#`). They can
also rotate clockwise or counterclockwise 90 degrees at a time (increasing
their score by `1000` points).

To figure out the best place to sit, you start by grabbing a map (your puzzle
input) from a nearby kiosk. For example:

    
    
    ###############
    #.......#....E#
    #.#.###.#.###.#
    #.....#.#...#.#
    #.###.#####.#.#
    #.#.#.......#.#
    #.#.#####.###.#
    #...........#.#
    ###.#.#####.#.#
    #...#.....#.#.#
    #.#.#.###.#.#.#
    #.....#...#.#.#
    #.###.#.#.#.#.#
    #S..#.....#...#
    ###############
    

There are many paths through this maze, but taking any of the best paths would
incur a score of only `_7036_`. This can be achieved by taking a total of `36`
steps forward and turning 90 degrees a total of `7` times:

    
    
    ###############
    #.......#.... _E_ #
    #.#.###.#.### _^_ #
    #.....#.#...# _^_ #
    #.###.#####.# _^_ #
    #.#.#.......# _^_ #
    #.#.#####.### _^_ #
    #.. _>_ _>_ _>_ _>_ _>_ _>_ _>_ _>_ _v_ # _^_ #
    ### _^_ #.##### _v_ # _^_ #
    # _>_ _>_ _^_ #.....# _v_ # _^_ #
    # _^_ #.#.###.# _v_ # _^_ #
    # _^_....#...# _v_ # _^_ #
    # _^_ ###.#.#.# _v_ # _^_ #
    #S..#.....# _>_ _>_ _^_ #
    ###############
    

Here's a second example:

    
    
    #################
    #...#...#...#..E#
    #.#.#.#.#.#.#.#.#
    #.#.#.#...#...#.#
    #.#.#.#.###.#.#.#
    #...#.#.#.....#.#
    #.#.#.#.#.#####.#
    #.#...#.#.#.....#
    #.#.#####.#.###.#
    #.#.#.......#...#
    #.#.###.#####.###
    #.#.#...#.....#.#
    #.#.#.#####.###.#
    #.#.#.........#.#
    #.#.#.#########.#
    #S#.............#
    #################
    

In this maze, the best paths cost `_11048_` points; following one such path
would look like this:

    
    
    #################
    #...#...#...#.. _E_ #
    #.#.#.#.#.#.#.# _^_ #
    #.#.#.#...#...# _^_ #
    #.#.#.#.###.#.# _^_ #
    # _>_ _>_ _v_ #.#.#.....# _^_ #
    # _^_ # _v_ #.#.#.##### _^_ #
    # _^_ # _v_..#.#.# _>_ _>_ _>_ _>_ _^_ #
    # _^_ # _v_ #####.# _^_ ###.#
    # _^_ # _v_ #.. _>_ _>_ _>_ _>_ _^_ #...#
    # _^_ # _v_ ### _^_ #####.###
    # _^_ # _v_ # _>_ _>_ _^_ #.....#.#
    # _^_ # _v_ # _^_ #####.###.#
    # _^_ # _v_ # _^_........#.#
    # _^_ # _v_ # _^_ #########.#
    #S# _>_ _>_ _^_..........#
    #################
    

Note that the path shown above includes one 90 degree turn as the very first
move, rotating the Reindeer from facing East to facing North.

Analyze your map carefully. _What is the lowest score a Reindeer could
possibly get?_

Your puzzle answer was `74392`.

## Part Two

Now that you know what the best paths look like, you can figure out the best
spot to sit.

Every non-wall tile (`S`, `.`, or `E`) is equipped with places to sit along
the edges of the tile. While determining which of these tiles would be the
best spot to sit depends on a whole bunch of factors (how comfortable the
seats are, how far away the bathrooms are, whether there's a pillar blocking
your view, etc.), the most important factor is _whether the tile is on one of
the best paths through the maze_. If you sit somewhere else, you'd miss all
the action!

So, you'll need to determine which tiles are part of _any_ best path through
the maze, including the `S` and `E` tiles.

In the first example, there are `_45_` tiles (marked `O`) that are part of at
least one of the various best paths through the maze:

    
    
    ###############
    #.......#.... _O_ #
    #.#.###.#.### _O_ #
    #.....#.#...# _O_ #
    #.###.#####.# _O_ #
    #.#.#.......# _O_ #
    #.#.#####.### _O_ #
    #.. _O_ _O_ _O_ _O_ _O_ _O_ _O_ _O_ _O_ # _O_ #
    ### _O_ # _O_ ##### _O_ # _O_ #
    # _O_ _O_ _O_ # _O_....# _O_ # _O_ #
    # _O_ # _O_ # _O_ ###.# _O_ # _O_ #
    # _O_ _O_ _O_ _O_ _O_ #...# _O_ # _O_ #
    # _O_ ###.#.#.# _O_ # _O_ #
    # _O_..#.....# _O_ _O_ _O_ #
    ###############
    

In the second example, there are `_64_` tiles that are part of at least one of
the best paths:

    
    
    #################
    #...#...#...#.. _O_ #
    #.#.#.#.#.#.#.# _O_ #
    #.#.#.#...#...# _O_ #
    #.#.#.#.###.#.# _O_ #
    # _O_ _O_ _O_ #.#.#.....# _O_ #
    # _O_ # _O_ #.#.#.##### _O_ #
    # _O_ # _O_..#.#.# _O_ _O_ _O_ _O_ _O_ #
    # _O_ # _O_ #####.# _O_ ### _O_ #
    # _O_ # _O_ #.. _O_ _O_ _O_ _O_ _O_ # _O_ _O_ _O_ #
    # _O_ # _O_ ### _O_ ##### _O_ ###
    # _O_ # _O_ # _O_ _O_ _O_ #.. _O_ _O_ _O_ #.#
    # _O_ # _O_ # _O_ ##### _O_ ###.#
    # _O_ # _O_ # _O_ _O_ _O_ _O_ _O_ _O_ _O_..#.#
    # _O_ # _O_ # _O_ #########.#
    # _O_ # _O_ _O_ _O_..........#
    #################
    

Analyze your map further. _How many tiles are part of at least one of the best
paths through the maze?_

Your puzzle answer was `426`.

Both parts of this puzzle are complete! They provide two gold stars: **

At this point, you should [return to your Advent calendar](https://adventofcode.com/2024) and try
another puzzle.

If you still want to see it, you can [get your puzzle input](https://adventofcode.com/2024/day/16/input).
