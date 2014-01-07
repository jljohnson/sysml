/**
 * 
 */
package sysml;

import gov.nasa.jpl.mbee.util.ClassUtils;
import gov.nasa.jpl.mbee.util.Debug;
import gov.nasa.jpl.mbee.util.Pair;
import gov.nasa.jpl.mbee.util.Utils;

import java.lang.Object;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import sysml.AbstractReference.Interpretation.Category;
import sysml.SystemModel.MethodCall;
import sysml.SystemModel.ModelItem;

/**
 * AbstractReference simply provides fields for supporting accessors in
 * Reference. Subclasses of AbstractReference must minimally redefine
 * makeReference(), getAlternatives(), and getItems().
 */
public abstract class AbstractReference< RT, SM extends SystemModel< O, C, T, P, N, I, U, R, V, W, CT >, O, C, T, P, N, I, U, R, V, W, CT > implements Reference< RT, SM > {

    // try to use ModelItem enum as interpretation 
    public static class Interpretation {
        public static enum Category { ModelItem, Related, RelatedSource, RelatedTarget, MULTIPLE, UNKNOWN };
        Category category = Category.UNKNOWN;
        SystemModel.ModelItem modelItemInterpretation = null;
        public Interpretation() {}
        public Interpretation( Category category,
                               ModelItem modelItemInterpretation ) {
            super();
            this.category = category;
            this.modelItemInterpretation = modelItemInterpretation;
        }
    }
    
    SM model = null;
    Object scope = null;
    Object specifier = null;
    Object nextSpecifier = null;
    Class< RT > type = null;
    Boolean isTemplate = null;
    
    Interpretation interpretation = new Interpretation();
    
    List< Reference< ? extends RT, SM > > alternatives = null;
    protected Collection< RT > items = null;
    
    public AbstractReference() {
    }

    public AbstractReference( SM model ) {
        this.model = model;
    }

    public AbstractReference( SM model,
                              Object scope,
                              Object specifier,
                              Object nextSpecifier,
                              Class< RT > type,
                              Boolean isTemplate ) {
        super();
        this.model = model;
        this.scope = scope;
        this.type = type;
        this.specifier = specifier;
        this.nextSpecifier = nextSpecifier;
        this.isTemplate = isTemplate;
    }

    @Override
    public abstract AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > makeReference();
//    {
//        return new AbstractReference< RT, SM >(); 
//    }

    @Override
    public  AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > makeReference( SM model ) {
        //return new AbstractReference< RT >( model );
        AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > r = makeReference();
        r.setModel( model );
        return r;
    }

    @Override
    public AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > makeReference( SM model,
                                           Object scope, Class< RT > type, Object specifier,
                                           Object nextSpecifier, boolean isTemplate ) {
        //return new AbstractReference< RT >( model, scope, type, specifier, nextSpecifier, isTemplate );
        AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > r = makeReference();
        r.setModel( model );
        r.setScope( scope );
        r.setType( type );
        r.setSpecifier( specifier );
        r.setNextSpecifier( nextSpecifier );
        r.setIsTemplate( isTemplate );
        return r;
    }
    
    public AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > clone() throws CloneNotSupportedException {
        //return new AbstractReference< RT >( model, scope, type, specifier, nextSpecifier, type, isTemplate );
        return makeReference( model, scope, type, specifier, nextSpecifier, isTemplate );
    }


    /* (non-Javadoc)
     * @see sysml.Reference#getModel()
     */
    @Override
    public SM getModel() {
        return this.model;
    }

    @Override
    public void setModel( SM model ) {
        this.model = model;
    }

    @Override
    public Object getScope() {
        return scope;
    }

    @Override
    public void setScope( Object scope ) {
        this.scope = scope;
    }

    @Override
    public Object getSpecifier() {
        return specifier;
    }

    @Override
    public void setSpecifier( Object specifier ) {
        this.specifier = specifier;
    }

    @Override
    public Class< RT > getType() {
        return type;
    }

    @Override
    public void setType( Class< RT > type ) {
        this.type = type;
    }

    @Override
    public Object getNextSpecifier() {
        return nextSpecifier;
    }

    @Override
    public void setNextSpecifier( Object nextSpecifier ) {
        this.nextSpecifier = nextSpecifier;
    }

    @Override
    public boolean isTemplate() {
        return isTemplate;
    }

    @Override
    public void setIsTemplate( boolean isTemplate ) {
        this.isTemplate = isTemplate;
    }

    /* (non-Javadoc)
     * @see sysml.Reference#isConsistent()
     */
    @Override
    public boolean isConsistent() {
        Collection< RT > items = getItems();
        if ( !isTemplate() && ( items == null || items.isEmpty() ) ) return false;
        if ( items != null && getType() != null ) {
            for ( RT item : items ) {
                if ( item != null && !getType().isInstance( item ) ) return false;
            }
        }
        return true;
    }

    @Override
    public List< Reference< ? extends RT, SM > > getAlternatives() {
        if ( alternatives == null ) {
            alternatives = new ArrayList< Reference< ? extends RT, SM > >();
        } else alternatives.clear();
        for ( Interpretation.Category c : Interpretation.Category.values() ) {
            SystemModel.ModelItem mi = null;
            Collection< RT > items;
            if ( c == Category.ModelItem ) {
                for ( SystemModel.ModelItem i : SystemModel.ModelItem.values() ) {
                    Interpretation interp = new Interpretation( c, i );
                    Reference<RT, SM> alternative = getReferenceForInterpretation( interp );
                    if ( alternative != null ) alternatives.add( alternative );
                }
            } else {
                
            }
            return alternatives;
        }

        // TODO
        return null;
    }
//  throw new UnsupportedOperationException();

    public AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT >
            getReferenceForInterpretation( Interpretation interp ) {
        Collection< RT > items = getItemsForInterpretation( interp );
        if ( !Utils.isNullOrEmpty( items ) ) {
            AbstractReference< RT, SM, O, C, T, P, N, I, U, R, V, W, CT > r;
            try {
                r = this.clone();
                r.interpretation = interp;
                r.setItems( items );
                return r;
            } catch ( CloneNotSupportedException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    protected void setItems( Collection< RT > items ) {
        this.items  = items;
    }

    protected static void sub( MethodCall methodCall, int indexOfArg, Object obj ) {
        if ( indexOfArg < 0 ) Debug.error("bad indexOfArg " + indexOfArg );
        else if ( indexOfArg == 0 ) methodCall.objectOfCall = obj;
        else if ( indexOfArg > methodCall.arguments.length ) Debug.error( "bad index "
                                                                          + indexOfArg
                                                                          + "; only "
                                                                          + methodCall.arguments.length
                                                                          + " arguments!" );
        else methodCall.arguments[indexOfArg-1] = obj;
    }

    /**
     * @param objects
     * @param methodCall
     * @param indexOfObjectArgument
     *            where in the list of arguments an object from the collection
     *            is substituted (1 to total number of args or 0 to indicate
     *            that the objects are each substituted for
     *            methodCall.objectOfCall).
     * @return the subset of objects for which the method call returns true
     */
    public static < XX > Collection<XX> filter( Collection< XX > objects,
                                                MethodCall methodCall,
                                                int indexOfObjectArgument ) {
        Collection< XX > coll = new ArrayList< XX >( objects );
        for ( XX o : objects ) {
            sub( methodCall, indexOfObjectArgument, o );
            Pair< Boolean, Object > result = methodCall.invoke();
            if ( result != null && result.first && Utils.isTrue( result.second, false ) ) {
                coll.add( o );
            }
        }
        return coll;
    }
    
    /**
     * @param objects
     * @param methodCall
     * @param indexOfObjectArgument
     *            where in the list of arguments an object from the collection
     *            is substituted (1 to total number of args or 0 to indicate
     *            that the objects are each substituted for
     *            methodCall.objectOfCall).
     * @return the results of the methodCall on each of the objects
     */
    public static < XX > Collection< XX > map( Collection< ? > objects,
                                               MethodCall methodCall,
                                               int indexOfObjectArgument ) {
        Collection< XX > coll = new ArrayList<XX>();
        for ( Object o : objects ) {
            sub( methodCall, indexOfObjectArgument, o );
            Pair< Boolean, Object > result = methodCall.invoke();
            if ( result != null && result.first ) {
                coll.add( (XX)result.second );
            }
        }
        return coll;
    }
    
    // TODO -- make a SystemModelUtils for this or move to SystemModel
    public N asName( Object o ) {
        N name = null;
        Pair< Boolean, N > resN = ClassUtils.coerce( o, getModel().getNameClass(), true );
        if ( resN.first ) name = resN.second;
        return name;
    }
    
    // TODO -- make a SystemModelUtils for this
    public I asId( Object o ) {
        I id = null;
        Pair< Boolean, I > resI = ClassUtils.coerce( o, getModel().getIdentifierClass(), true );
        if ( resI.first ) id = resI.second;
        return id;
    }

    // TODO -- make a SystemModelUtils for this
    /**
     * @param o
     * @return a conversion of the java.lang.Object to a SystemModel object or null
     */
    public O asObject( Object o ) {
        O object = null;
        Pair< Boolean, O > resI = ClassUtils.coerce( o, getModel().getObjectClass(), true );
        if ( resI.first ) object = resI.second;
        return object;
    }
    
    /**
     * @param object
     * @return a Collection of contexts including either the object as a context
     *         or the contexts in the object as a Collection.
     */
    public Collection<C> asContext( Object object ) {
        if ( object == null ) return null;
        Pair< Boolean, List< C > > result =
                ClassUtils.coerceList( object, getModel().getContextClass(),
                                       true );
        return result.second;
    }

    /**
     * @return a Collection of contexts as can be derived from the scope. 
     */
    public Collection<C> getScopeAsContext() {
        Object scope = getScope();
        return asContext( scope );
    }
    
    protected Collection< RT > getObjectForRole( N role ) {
        Collection< RT > theItems = null;
        O scopeObject = asObject( getScope() );
        Collection< R > rels =
                getModel().getRelationships( scopeObject,
                                             asName( getSpecifier() ),
                                             null );
        //theItems = Utils.asList( rels, getType() );
        if ( Utils.isNullOrEmpty( rels ) ) {
            String s = getSpecifier().toString().toLowerCase();
            if ( s.contains( "relation" ) || s.contains("source") ) {
                rels = getModel().getRelationships( scopeObject, null );
                Method method;
                try {
                    method = getModel().getClass().getMethod( "getSource()",
                                                     new Class< ? >[] {} );
                    SystemModel.MethodCall methodCall =
                            new SystemModel.MethodCall( getModel(), method,
                                                        new Object[]{} );

                    Collection< Object > related = map( rels, methodCall, 0 );
                    theItems = Utils.asList( related, getType() );
                } catch ( NoSuchMethodException e ) {
                    e.printStackTrace();
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return theItems;
        
    }
    
    public Collection< RT > getItemsForInterpretation( Interpretation i ) {
        O scopeObject = null;
        Pair< Boolean, O > resO = null;
        Collection< RT > theItems = null;
        Collection< O > related = null;
        switch ( i.category ) {
            case UNKNOWN:
                return Collections.emptyList();
            case Related:
////                    getItemsForInterpretation( new Interpretation( Category.ModelItem, ModelItem.RELATIONSHIP ) );
////                if ( items == null ) return Collections.emptyList();
////                for ( RT o : items ) {
//                resO = ClassUtils.coerce( getScope(),
//                                          getModel().getObjectClass(), true );
//                scopeObject = resO.first ? resO.second : (O)getScope(); // REVIEW -- potential ClassCastException!
                scopeObject = asObject( getScope() );
                related = getModel().getRelated( scopeObject,
                                                 asName( getSpecifier() ),
                                                 null );
                theItems = Utils.asList( related, getType() ); // REVIEW -- need to use coerce instead?
                //                }
//                SystemModel.MethodCall methodCall =
//                        new SystemModel.MethodCall( getModel(),
//                                                    getType().getMethod( "isInstance",
//                                                                         new Class<?>[] { Object.class } ),
//                                                                         new Object[]{ null } );
//                Collection< O > objects;
//                Collection< Object > otherItems = getModel().map( objects, methodCall, 0000 );
//                methodCall =
//                        new SystemModel.MethodCall( getType(),
//                                                    getType().getMethod( "isInstance",
//                                                                         new Class<?>[] { Object.class } ),
//                                                    new Object[]{ null } );
//                Collection< RT > filterResults = filter( items, methodCall, 0 );
//                return filterResults;
                return theItems;
//                break;
            case RelatedSource:
//                resO = ClassUtils.coerce( getScope(),
//                                          getModel().getObjectClass(), true );
//                scopeObject = resO.first ? resO.second : (O)getScope(); // REVIEW -- potential ClassCastException!
                scopeObject = asObject( getScope() );
                Collection< R > rels =
                        getModel().getRelationships( scopeObject,
                                                     asName( getSpecifier() ),
                                                     null );
                //theItems = Utils.asList( rels, getType() );
                if ( Utils.isNullOrEmpty( rels ) ) {
                    String s = getSpecifier().toString().toLowerCase();
                    if ( s.contains( "relation" ) || s.contains("source") ) {
                        rels = getModel().getRelationships( scopeObject, null );
                        Method method;
                        try {
                            method = getModel().getClass().getMethod( "getSource()",
                                                             new Class< ? >[] {} );
                            SystemModel.MethodCall methodCall =
                                    new SystemModel.MethodCall( getModel(), method,
                                                                new Object[]{} );

                            related = map( rels, methodCall, 0 );
                            theItems = Utils.asList( related, getType() );
                        } catch ( NoSuchMethodException e ) {
                            e.printStackTrace();
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                return theItems;
            case RelatedTarget:
                // TODO
                break;
            case ModelItem:
                if ( i.modelItemInterpretation == null ) return Collections.emptyList();
                Collection< RT > items = null;
                Collection< ModelItem > itemTypes = Utils.newList( i.modelItemInterpretation );
                Collection< C > context = getScopeAsContext();
                if ( Utils.isNullOrEmpty( context ) ) {
                    context = getModel().getContext(); 
                }
                I id = null;
                Pair< Boolean, I > resI = ClassUtils.coerce( getSpecifier(), getModel().getIdentifierClass(), true );
                if ( resI.first ) id = resI.second; 
                N name = null;
                Pair< Boolean, N > resN = ClassUtils.coerce( getSpecifier(), getModel().getNameClass(), true );
                if ( resN.first ) name = resN.second;
                Object result = getModel().op( SystemModel.Operation.READ, itemTypes, context, id, name, null, false );
                Pair< Boolean, List< RT > > resL = ClassUtils.coerceList( result, getType(), true );
                if ( resL.first ) {
                    items = resL.second;
                }
                return items;
//                switch( i.modelItemInterpretation ) {
//                    case CONSTRAINT:
//                    //{HERE!!!;} // TODO
//                        break;
//                    case CONTEXT:
//                    case IDENTIFIER:
//                    case NAME:
//                    case OBJECT:
//                    case PROPERTY:
//                    case RELATIONSHIP:
//                    case TYPE:
//                    case VALUE:
//                    case VERSION:
//                    case VIEW:
//                    case VIEWPOINT:
//                    case WORKSPACE:
//                    default:
//                    // TODO
//                };
            default:
                // TODO -- ERROR!
        };
        return Collections.emptyList();
    }
    
    @Override
    public Collection< RT > getItems() {
        // TOOD
        return null;
    }

}
