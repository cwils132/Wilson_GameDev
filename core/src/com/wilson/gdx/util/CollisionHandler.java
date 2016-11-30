package com.wilson.gdx.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.ObjectMap;
import com.wilson.gdx.game.WorldController;
import com.wilson.gdx.game.Assets;
import com.wilson.gdx.game.objects.AbstractGameObject;
import com.wilson.gdx.game.objects.Rock;
import com.wilson.gdx.game.objects.BunnyHead;
import com.wilson.gdx.game.objects.BunnyHead.JUMP_STATE;
import com.wilson.gdx.game.objects.GoldCoin;
import com.wilson.gdx.game.Level;


/**
 * This class is to handle all collisions for Box2D. We only need to worry about Rocks'
 * and the Player at the moment so that is all it will be checking against.
 * @author Chris
 *
 */
public class CollisionHandler implements ContactListener
{
    private ObjectMap<Short, ObjectMap<Short, ContactListener>> listeners;

    private WorldController world;

    public CollisionHandler(WorldController w)
    {
    	world = w;
        listeners = new ObjectMap<Short, ObjectMap<Short, ContactListener>>();
    }
    
    /**
     * Adds listeners to check if contact occurs between the Player
     * and another object.
     * @param categoryA
     * @param categoryB
     * @param listener
     */
    public void addListener(short categoryA, short categoryB, ContactListener listener)
    {
        addListenerInternal(categoryA, categoryB, listener);
        addListenerInternal(categoryB, categoryA, listener);
    }

    /**
     * If a contact occurs it is handeled.
     */
    @Override
    public void beginContact(Contact contact)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        //Gdx.app.log("CollisionHandler-begin A", "begin");

       // processContact(contact);

        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.beginContact(contact);
        }
    }

    /**
     * Resolves contacts between objects
     */
    @Override
    public void endContact(Contact contact)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

       // Gdx.app.log("CollisionHandler-end A", "end");
        processContact(contact);

        // Gdx.app.log("CollisionHandler-end A", fixtureA.getBody().getLinearVelocity().x+" : "+fixtureA.getBody().getLinearVelocity().y);
        // Gdx.app.log("CollisionHandler-end B", fixtureB.getBody().getLinearVelocity().x+" : "+fixtureB.getBody().getLinearVelocity().y);
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.endContact(contact);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.preSolve(contact, oldManifold);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse)
    {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        ContactListener listener = getListener(fixtureA.getFilterData().categoryBits, fixtureB.getFilterData().categoryBits);
        if (listener != null)
        {
            listener.postSolve(contact, impulse);
        }
    }

    private void addListenerInternal(short categoryA, short categoryB, ContactListener listener)
    {
        ObjectMap<Short, ContactListener> listenerCollection = listeners.get(categoryA);
        if (listenerCollection == null)
        {
            listenerCollection = new ObjectMap<Short, ContactListener>();
            listeners.put(categoryA, listenerCollection);
        }
        listenerCollection.put(categoryB, listener);
    }

    private ContactListener getListener(short categoryA, short categoryB)
    {
        ObjectMap<Short, ContactListener> listenerCollection = listeners.get(categoryA);
        if (listenerCollection == null)
        {
            return null;
        }
        return listenerCollection.get(categoryB);
    }

    /**
     * Processes contact within game.
     * @param contact
     */
    private void processContact(Contact contact)
    {
    	Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();
        AbstractGameObject objA = (AbstractGameObject)fixtureA.getBody().getUserData();
        AbstractGameObject objB = (AbstractGameObject)fixtureB.getBody().getUserData();

        if (objA instanceof BunnyHead)
        {
        	processPlayerContact(fixtureA, fixtureB);
        }
        else if (objB instanceof BunnyHead)
        {
        	processPlayerContact(fixtureB, fixtureA);
        }
    }

    /**
     * If player comes in contact with the rocks the jump state is set to grounded
     * If the player picks up a coin, jumps, or loses  a life, a sound is played.
     * @param playerFixture
     * @param objFixture
     */
    private void processPlayerContact(Fixture playerFixture, Fixture objFixture)
    {
        if (objFixture.getBody().getUserData() instanceof Rock)
        {
        	BunnyHead player = (BunnyHead) playerFixture.getBody().getUserData();
            player.acceleration.y = 0;
            player.velocity.y = 0;
            player.jumpState = JUMP_STATE.GROUNDED;
            playerFixture.getBody().setLinearVelocity(player.velocity);
        }
        
    	else if (objFixture.getBody().getUserData() instanceof GoldCoin)
        {
            AudioManager.instance.play(Assets.instance.sounds.pickupCoin);
            AudioManager.instance.play(Assets.instance.sounds.jump);
            AudioManager.instance.play(Assets.instance.sounds.liveLost);

            GoldCoin book = (GoldCoin) objFixture.getBody().getUserData();
            world.score = +book.getScore();
            world.flagForRemoval(book);
        }
    }

}