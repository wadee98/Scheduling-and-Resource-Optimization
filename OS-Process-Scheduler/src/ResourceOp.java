class ResourceOp extends Action {
    int resourceId, amount;
    boolean isRequest; // true for Request (R), false for Release (F)
    ResourceOp(boolean isRequest, int resourceId, int amount) {
        this.isRequest = isRequest;
        this.resourceId = resourceId;
        this.amount = amount;
    }    
    ResourceOp(){}
    @Override
    public String toString() {
        return (isRequest ? "R" : "F") + "[" + resourceId + "," + amount + "]";
    }
}