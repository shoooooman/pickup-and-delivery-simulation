import jbotsim.Color;

public class GhostNode extends AbstractGridNode {
    // does not use requesting and locking

    @Override
    public void onStart() {
        setID(id++);
        path = pathGen.newPath(getID(), this.getLocation());
        // when init point is the same as dest, path will be empty
        if (!path.isEmpty()) {
            dest = path.getLast();
        } else {
            dest = getLocation();
        }
        next = prev = this.getLocation();
    }

    @Override
    public void onClock() {
        this.setColor(Color.blue);

        GridPoint here = this.getLocation();

        // when arriving at a grid point
        if (here.equals(next)) {
            path.remove(next);

            /* for debugging */
            GRID_LOG(here);
            /* end debugging */

            // when reaching the destination
            if (here.equals(dest)) {
                /* for debugging */
                GRID_LOG("complete!");
                /* end debugging */

                // the number of tasks this node completed
                numTask++;

                this.setColor(Color.green);

                path = pathGen.newPath(getID(), this.getLocation());
                if (!path.isEmpty()) {
                    dest = path.getLast();
                } else {
                    dest = getLocation();
                }
            }

            if (!here.equals(prev)) {
                assert(!prev.equals(next));
                prev = next;
            }

            if (path.size() >= 1) {
                this.setColor(Color.red);
                next = path.element();
                setDirection(next);
            }
        }
    }
}
