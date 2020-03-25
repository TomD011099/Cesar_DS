public class NodeName {
    private String name;

    public NodeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeName nodeName = (NodeName) o;

        return name.equals(nodeName.name);
    }

    @Override
    public int hashCode() {
        int hash = name.hashCode();
        while ((hash < 0) || (hash > 32768)) {
            if (hash < 0) {
                hash += 32768;
            } else {
                hash -= 32768;
            }
        }
        return hash;
    }
}
