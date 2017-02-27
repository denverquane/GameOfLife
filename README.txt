Denver Quane
ID# 101611184
CS - 351
Conway's Game Of Life Program

Summary:

This program simulates Conway's Game of Life on a large (up to 10,000 x 10,000) grid, utilizing multithreading to
update the board faster than with a single thread. The program utilizes a Graphical User Interface (GUI) to allow the user
to specify the number of threads to utilize, the size of the board, and much more runtime functionality
(See "Special Features").

Controls:

Upon launching the simulation of a certain size with a specified number of threads, the user is then free to control
the simulation of Conway's Game Of Life as they like.

    Canvas Controls:
        - Left Click (when Paused):
            Toggles a cell's status from alive to dead or vice versa.

        - Shift + Left Click Drag (when Paused):
            Activate all cells underneath the mouse as it is dragged
            (note that this does not toggle the Cell status, but rather force them to Alive).

        - Left Click Drag:
            Pan the camera across the game board.

        - Right Click (when Paused):
            Toggles a flag at the current mouse location. Refer to "Special Features" or "Controls" to
            see the functionality associated with placing flags.

        - Mouse Wheel Up/Down:
            Zooms the camera to the current mouse position, recentering mouse position to be in the screen center

     Other Controls:
        By GUI Location:
            TOP
            - Preset Buttons:
                Clears the board and loads the named preset onto the board

            LEFT
            - Play:
                This toggle button starts, or pauses the current simulation's updating process.

            - Simulate Frame:
                This button simulates a single updating of the board, whereas "Play" updates continuously.

            - Reset Board:
                This button wipes the entire board of any alive cells.

            - Reset Camera:
                This button restores the camera to it's default position at the top left of the game board,
                and also resets the zoom factor to it's default (each cell = 2 pixels in size).

            - Clear Flags:
                This button removes all currently placed flags from the game board.

             - Frames Per Second (FPS):
                Displays how many times the Canvas is being redrawn

             - Updates Per Second:
                How many times the board is being updated completely every second

            BOTTOM
            - Save Pattern:
                This text field allows you to enter the filename of the pattern that
                will be saved by the current camera view. The current view is automatically trimmed to the minimum size,
                and stored in the requested location and under the specified name, with the extension ".rle"
                (Run Length Encoding).
                NOTE: If the program is run as an executable Jar, then these files will always (by .jar requirement)
                be placed in the same directory as the .jar, instead of within the .jar itself.

             - Load Pattern:
                Loads a specified .rle pattern from the data/patterns directory within the .jar, to all currently placed
                flags on the board. If the user requests a pattern that has been previously saved, then the program
                will also seek out the pattern outside of the .jar file. Some patterns are located directly within the
                "patterns" folder, while others are located under the "alpha" subdirectory (Patterns comprising the
                alphabet), or under "all" for every single .RLE file found on the Conway's Game Of Life Wiki.

                ... No seriously, check it out, there's thousands of patterns in there, it's dope.

              - Example Patterns:
                These buttons provide some examples of the patterns already available for loading. Pressing any of these
                buttons will load the pattern onto all currently placed flags on the board
                The top two rows of buttons provide patterns found directly in the "patterns" directory, but the bottom
                two buttons offer examples of the paths required to access the deeper nested patterns, such as in the all
                or alpha directories.

               - Enter Coordinates:
                Allows the user to specify coordinates to flag on the board, instead of by selecting with the mouse.
                Allows for more granular, and precise control over placing patterns (and faster, considering pressing
                "Enter" while entering coordinates will load the flag without pressing the button).

            RIGHT
                - Coordinates
                 Provides the current cell Coordinates of the mouse cursor.
                 Can be edited (press Enter afterwards) to manually set the new Camera location to move to.

                - Camera Speed:
                 Allows the user to select the speed at which the camera pans around the board. The controls are provided
                 in units of cells, where 1 indicates that a mouse drag will move the camera by a minimum of 1 cell per
                 drag.

Special Features:

    - User-specified size of board, or number of threads to run the program
    - Resizing of the canvas in response to window size changes, and resizing of the current camera view of the board
    - Performance displays in terms of graphical frames per second, and complete board updates per second
    - Loading of thousands of .rle files, both through a button interface and by specifying filenames and directories
    - Saving of interesting or noteworthy patterns, that can then be loaded later through the same above loading interface
    - Flagging of any location on the board for loading patterns at precise points, and flag wiping if incorrectly placed
    - Precise flagging of cell locations by specifying cell coordinates
    - Example patterns accessible via buttons for those less inclined to type filenames and paths
    - Zooming to specific cell locations and automatic recentering
    - Camera panning and Camera location/zoom reset functionality
    - Graphical information regarding the current mouse position for greater precision
    - Ability to enter coordinates to manually move the camera to a specific coordinate
    - Gridline displays between cells when the size of cells becomes large enough (high enough zoom)
    - Adjustable Camera movement speed
    - Color gradient between light green and blue for the age of a cell (blue = oldest cell)


Notable Patterns to Load:

    - "all/pufferfishbreeder"
        Large moving ship that spawns vertically-moving "pufferfish" as it moves. See "all/pufferfish" as well!

    - "heart"
        Small heart shape that collapses and flies off as 2 gliders! (I found this one)

    - "repeatGliders"
        5 gosper gliders in a row

    - "bigGlider"
        Large diagonally-moving glider

    - "alpha/h"
        "alpha/" contains all letters, but the "h" letter has a particularly interesting pattern when ran!

    - "all/honeythieveswithtesttubebaby"
        I mainly just love the name for this one!

    - "all/infinitegliderhotel2"
        Really interesting pattern!

    - "all/koksgalaxy_synth"
        Really fascinating patterns within this one.

    - "all/twinprimecalculator"
        Amazing, from what I understand it counts numbers from 1, and sends off gliders for one's that are prime

    - "all/vgun"
        Really big (2.3k x 1.4k) factory of spaceships!

    - "all/period59gun"
        WARNING: This one is just over 4k x 4k, and will freeze the program momentarily when loading, but it produces
        gliders really fast, and is generally mesmerizing to watch

    - "all/blockstacker"
        Makes an infinitely-tall stack of 2x2 blocks!


Known Issues:

  - PLEASE don't send the coordinate or filename boxes arguments other than what they expect!
    It shouldn't crash the program, but I didn't have the time
    (or motivation) to filter the input extensively. The coordinate boxes expect positive numbers just as the
    saving/loading text boxes expect valid character strings for file names. If these were required parts of the program,
    I certainly wouldn't have skimped on the edge case tests, but as they are custom functionality for a
    project due after a fairly short development window, I didn't bother accounting for malevolent users!

  - If the program can't find a file, inside or outside the jar, it only tells the user in the console

  - Same applies for saving: the only confirmation message occurs in console



Thank you for reading, and enjoy the program!!! I sure did enjoy making it and adding so much functionality!

- Denver Quane



