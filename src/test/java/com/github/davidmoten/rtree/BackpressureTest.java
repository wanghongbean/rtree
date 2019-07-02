package com.github.davidmoten.rtree;

import static com.github.davidmoten.rtree.RTreeTest.e;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.github.davidmoten.junit.Asserts;
import com.github.davidmoten.rtree.FlowableSearch.SearchSubscription;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.internal.util.ImmutableStack;

import io.reactivex.functions.Predicate;

public class BackpressureTest {

    @Test
    public void testConstructorIsPrivate() {
        Asserts.assertIsUtilityClass(Backpressure.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBackpressureSearch() throws Exception {
        Subscriber<Object> sub = Mockito.mock(Subscriber.class);
        ImmutableStack<NodePosition<Object, Geometry>> stack = ImmutableStack.empty();
        Predicate<Geometry> condition = Mockito.mock(Predicate.class);
        Node<Object, Geometry> node = Mockito.mock(Node.class);
        SearchSubscription<Object, Geometry> ss = new SearchSubscription<>(node, condition,sub);
        Backpressure.search(condition, sub, stack, 1, ss);
        Mockito.verify(sub, Mockito.never()).onNext(Mockito.any());
    }

    @Test
    public void testBackpressureSearchNodeWithConditionThatAlwaysReturnsFalse() {
        RTree<Object, Rectangle> tree = RTree.maxChildren(3).<Object, Rectangle> create()
                .add(e(1)).add(e(3)).add(e(5)).add(e(7));

        Set<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.search(e(1).geometry()).subscribe(backpressureSubscriber(found));
        assertEquals(1, found.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRequestZero() throws Exception {
        Subscriber<Object> sub = new Subscriber<Object>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Object t) {

            }

            @Override
            public void onSubscribe(Subscription s) {
                // TODO Auto-generated method stub
                
            }
        };
        sub.onSubscribe(new Subscription() {
            
            @Override
            public void request(long n) {
                // TODO Auto-generated method stub
                
            }
            

            @Override
            public void cancel() {
            }

            
        });
        Node<Object, Geometry> node = Mockito.mock(Node.class);
        NodePosition<Object, Geometry> np = new NodePosition<Object, Geometry>(node, 1);
        ImmutableStack<NodePosition<Object, Geometry>> stack = ImmutableStack
                .<NodePosition<Object, Geometry>> empty().push(np);
        Predicate<Geometry> condition = Mockito.mock(Predicate.class);
        SearchSubscription<Object, Geometry> ss = new SearchSubscription<>(node, condition,sub);
        ImmutableStack<NodePosition<Object, Geometry>> stack2 = Backpressure.search(condition, sub,
                stack, 0, ss);
        assertTrue(stack2 == stack);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRequestZeroWhenUnsubscribed() {
        Subscriber<Object> sub = new Subscriber<Object>() {

            @Override
            public void onComplete() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(Object t) {

            }

            @Override
            public void onSubscribe(Subscription s) {
                
            }
        };
        sub.onSubscribe(new Subscription() {

            @Override
            public void request(long n) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void cancel() {
                // TODO Auto-generated method stub
                
            }
        });
        sub.cancel();
        Node<Object, Geometry> node = Mockito.mock(Node.class);
        NodePosition<Object, Geometry> np = new NodePosition<Object, Geometry>(node, 1);
        ImmutableStack<NodePosition<Object, Geometry>> stack = ImmutableStack
                .<NodePosition<Object, Geometry>> empty().push(np);
        Predicate<Geometry> condition = Mockito.mock(Func1.class);
        ImmutableStack<NodePosition<Object, Geometry>> stack2 = Backpressure.search(condition, sub,
                stack, 1);
        assertTrue(stack2.isEmpty());
    }

    @Test
    public void testBackpressureIterateWhenNodeHasMaxChildrenAndIsRoot() {
        Entry<Object, Rectangle> e1 = RTreeTest.e(1);
        @SuppressWarnings("unchecked")
        List<Entry<Object, Rectangle>> list = Arrays.asList(e1, e1, e1, e1);
        RTree<Object, Rectangle> tree = RTree.star().maxChildren(4).<Object, Rectangle> create()
                .add(list);
        HashSet<Entry<Object, Rectangle>> expected = new HashSet<Entry<Object, Rectangle>>(list);
        final HashSet<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.entries().subscribe(backpressureSubscriber(found));
        assertEquals(expected, found);
    }

    @Test
    public void testBackpressureRequestZero() {
        Entry<Object, Rectangle> e1 = RTreeTest.e(1);
        @SuppressWarnings("unchecked")
        List<Entry<Object, Rectangle>> list = Arrays.asList(e1, e1, e1, e1);
        RTree<Object, Rectangle> tree = RTree.star().maxChildren(4).<Object, Rectangle> create()
                .add(list);
        HashSet<Entry<Object, Rectangle>> expected = new HashSet<Entry<Object, Rectangle>>(list);
        final HashSet<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.entries().subscribe(new Subscriber<Entry<Object, Rectangle>>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Entry<Object, Rectangle> t) {
                found.add(t);
                request(0);
            }
        });
        assertEquals(expected, found);
    }

    @Test
    public void testBackpressureIterateWhenNodeHasMaxChildrenAndIsNotRoot() {
        Entry<Object, Rectangle> e1 = RTreeTest.e(1);
        List<Entry<Object, Rectangle>> list = new ArrayList<Entry<Object, Rectangle>>();
        for (int i = 1; i <= 17; i++)
            list.add(e1);
        RTree<Object, Rectangle> tree = RTree.star().maxChildren(4).<Object, Rectangle> create()
                .add(list);
        HashSet<Entry<Object, Rectangle>> expected = new HashSet<Entry<Object, Rectangle>>(list);
        final HashSet<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.entries().subscribe(backpressureSubscriber(found));
        assertEquals(expected, found);
    }

    @Test
    public void testBackpressureIterateWhenConditionFailsAgainstNonLeafNode() {
        Entry<Object, Rectangle> e1 = e(1);
        List<Entry<Object, Rectangle>> list = new ArrayList<Entry<Object, Rectangle>>();
        for (int i = 1; i <= 17; i++)
            list.add(e1);
        list.add(e(2));
        RTree<Object, Rectangle> tree = RTree.star().maxChildren(4).<Object, Rectangle> create()
                .add(list);
        HashSet<Entry<Object, Rectangle>> expected = new HashSet<Entry<Object, Rectangle>>(list);
        final HashSet<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.entries().subscribe(backpressureSubscriber(found));
        assertEquals(expected, found);
    }

    @Test
    public void testBackpressureIterateWhenConditionFailsAgainstLeafNode() {
        Entry<Object, Rectangle> e3 = e(3);
        RTree<Object, Rectangle> tree = RTree.star().maxChildren(4).<Object, Rectangle> create()
                .add(e(1)).add(e3);
        Set<Entry<Object, Rectangle>> expected = Collections.singleton(e3);
        final Set<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.search(e3.geometry()).subscribe(backpressureSubscriber(found));
        assertEquals(expected, found);
    }

    @Test
    public void testBackpressureFastPathNotInitiatedTwice() {
        Entry<Object, Rectangle> e3 = e(3);
        RTree<Object, Rectangle> tree = RTree.star().maxChildren(4).<Object, Rectangle> create()
                .add(e(1)).add(e3);
        Set<Entry<Object, Rectangle>> expected = Collections.singleton(e3);
        final Set<Entry<Object, Rectangle>> found = new HashSet<Entry<Object, Rectangle>>();
        tree.search(e3.geometry()).subscribe(new Subscriber<Entry<Object, Rectangle>>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Entry<Object, Rectangle> t) {
                found.add(t);
                request(Long.MAX_VALUE);
            }
        });
        assertEquals(expected, found);
    }

    private static Subscriber<Entry<Object, Rectangle>> backpressureSubscriber(
            final Set<Entry<Object, Rectangle>> found) {
        return new Subscriber<Entry<Object, Rectangle>>() {

            @Override
            public void onStart() {
                request(1);
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Entry<Object, Rectangle> t) {
                found.add(t);
                request(1);
            }
        };
    }

}
