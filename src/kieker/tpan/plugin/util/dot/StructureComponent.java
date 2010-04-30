package kieker.tpan.plugin.util.dot;

/**
 * This class implements unique functionality for components,
 * which work as normal {@link StructureElement}s otherwise.
 * 
 * Components usually have a {@link StructureDeploymentContext} as parent,
 * they have {@link StructureOperation}s as children,
 * and they are linked among each other with a <em>uses</em> relation.
 * 
 * @author Nina
 */
class StructureComponent extends StructureElement {

private String name;

StructureComponent( String name, StructureDeploymentContext parent, boolean isRoot ){
    super( parent, isRoot );
    this.name = name;
}

public String getName(){
    return name;
}

public String getShortName(){
    int lastDot = name.lastIndexOf( "." );
    if( lastDot < 0 ){
        return name;
    }
    return name.substring( lastDot );
}

/**
 * Checks a selection of internal variables for consistency.
 * @return true if all subordinate checks return true
 */
@Override
boolean check(){
    boolean result = true;
    if( !super.check() ){
        Util.writeOut( Util.LEVEL_WARNING, "< WARNING: structure element internal check failed." );
        return false;
    }
    if( getName().isEmpty() ){
        Util.writeOut( Util.LEVEL_WARNING, "< WARNING: component name empty." );
        result = false;
    }
    if( parent == null ){
        Util.writeOut( Util.LEVEL_WARNING, "< WARNING: component " + getName() + " without deployment context." );
        result = false;
    }
    if( causeRating < -1.0 || causeRating > 1.0 ){
        Util.writeOut( Util.LEVEL_WARNING, "< WARNING: component " + getName() + " has weird cause rating: " + causeRating );
        result = false;
    }
    if( percent < 0.0 || percent > 100.0 ){
        Util.writeOut( Util.LEVEL_WARNING, "< WARNING: component " + getName() + " has weird percent rating: " + percent );
        result = false;
    }
    int value;
    for( Integer opCount : incoming.values() ){
        value = opCount.intValue();
        if( value < 1 ){
            Util.writeOut( Util.LEVEL_WARNING, "< WARNING: component " + getName() + " has weird incoming connection count: " + value );
            result = false;
        }
    }
    for( Integer opCount : outgoing.values() ){
        value = opCount.intValue();
        if( value < 1 ){
            Util.writeOut( Util.LEVEL_WARNING, "< WARNING: component " + getName() + " has weird outgoing connection count: " + value );
            result = false;
        }
    }
    if( root ){
        if( outgoing.isEmpty() || outgoingRel.isEmpty() ){
            Util.writeOut( Util.LEVEL_WARNING, "< WARNING: root component " + getName() + " has no outgoing connections." );
            result = false;
        }
    }
    else if( incoming.isEmpty() || incomingRel.isEmpty() ){
        Util.writeOut( Util.LEVEL_WARNING, "< WARNING: non-root component " + getName() + " has no incoming connections." );
        result = false;
    }
    for( StructureElement op : children ){
        if( !op.check() ){
            Util.writeOut( Util.LEVEL_WARNING, "< WARNING: operation " + op.getName() + " internal check failed." );
            result = false;
        }
    }
    return result;
}

}
