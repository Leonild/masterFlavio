/**
 * =========================================================================
 * 					Bench4Q version 1.0.0
 * =========================================================================
 * 
 * Bench4Q is available on the Internet at http://forge.ow2.org/projects/jaspte
 * You can find latest version there. 
 * 
 * Distributed according to the GNU Lesser General Public Licence. 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by   
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 * 
 * SEE Copyright.txt FOR FULL COPYRIGHT INFORMATION.
 * 
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 *
 * This version is a based on the implementation of TPC-W from University of Wisconsin. 
 * This version used some source code of The Grinder.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 *  * Initial developer(s): Zhiquan Duan.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * 
 */
package org.bench4Q.console.ui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to create a tree structure of objects. Each element in the
 * tree is also a key to the next node down in the tree. It provides many ways
 * to add objects and branches, as well as many ways to retrieve.
 * <p>
 * HashTree implements the Map interface for convenience reasons. The main
 * difference between a Map and a HashTree is that the HashTree organizes the
 * data into a recursive tree structure, and provides the means to manipulate
 * that structure.
 * <p>
 * Of special interest is the {@link #traverse(HashTreeTraverser)} method, which
 * provides an expedient way to traverse any HashTree by implementing the
 * {@link HashTreeTraverser} interface in order to perform some operation on the
 * tree, or to extract information from the tree.
 * 
 * @see HashTreeTraverser
 * @see SearchByClass
 */
public class HashTree implements Serializable, Map, Cloneable {

	// GetLoggerForClass() uses ClassContext, which
	// causes a Security violation in RemoteJMeterImpl
	// so we use getLoggerFor() instead
	// private static final Logger log =
	// LoggingManager.getLoggerFor(HashTree.class.getName());

	// Used for the RuntimeException to short-circuit the traversal
	private static final String FOUND = "found"; // $NON-NLS-1$

	protected final Map data;

	/**
	 * Creates an empty new HashTree.
	 */
	public HashTree() {
		data = new HashMap();
	}

	/**
	 * Allow subclasses to provide their own Map.
	 */
	protected HashTree(Map _map) {
		data = _map;
	}

	/**
	 * Creates a new HashTree and adds the given object as a top-level node.
	 * 
	 * @param key
	 */
	public HashTree(Object key) {
		data = new HashMap();
		data.put(key, new HashTree());
	}

	/**
	 * The Map given must also be a HashTree, otherwise an
	 * UnsupportedOperationException is thrown. If it is a HashTree, this is
	 * like calling the add(HashTree) method.
	 * 
	 * @see #add(HashTree)
	 * @see java.util.Map#putAll(Map)
	 */
	public void putAll(Map map) {
		if (map instanceof HashTree) {
			this.add((HashTree) map);
		} else {
			throw new UnsupportedOperationException("can only putAll other HashTree objects");
		}
	}

	/**
	 * Exists to satisfy the Map interface.
	 * 
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet() {
		return data.entrySet();
	}

	/**
	 * Implemented as required by the Map interface, but is not very useful
	 * here. All 'values' in a HashTree are HashTree's themselves.
	 * 
	 * @param value
	 *            Object to be tested as a value.
	 * @return True if the HashTree contains the value, false otherwise.
	 * @see java.util.Map#containsValue(Object)
	 */
	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	/**
	 * This is the same as calling HashTree.add(key,value).
	 * 
	 * @param key
	 *            to use
	 * @param value
	 *            to store against key
	 * @see java.util.Map#put(Object, Object)
	 */
	public Object put(Object key, Object value) {
		Object previous = data.get(key);
		add(key, value);
		return previous;
	}

	/**
	 * Clears the HashTree of all contents.
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		data.clear();
	}

	/**
	 * Returns a collection of all the sub-trees of the current tree.
	 * 
	 * @see java.util.Map#values()
	 */
	public Collection values() {
		return data.values();
	}

	/**
	 * Adds a key as a node at the current level and then adds the given
	 * HashTree to that new node.
	 * 
	 * @param key
	 *            key to create in this tree
	 * @param subTree
	 *            sub tree to add to the node created for the first argument.
	 */
	public void add(Object key, HashTree subTree) {
		add(key);
		getTree(key).add(subTree);
	}

	/**
	 * Adds all the nodes and branches of the given tree to this tree. Is like
	 * merging two trees. Duplicates are ignored.
	 * 
	 * @param newTree
	 */
	public void add(HashTree newTree) {
		Iterator iter = newTree.list().iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			add(item);
			getTree(item).add(newTree.getTree(item));
		}
	}

	/**
	 * Creates a new HashTree and adds all the objects in the given collection
	 * as top-level nodes in the tree.
	 * 
	 * @param keys
	 *            a collection of objects to be added to the created HashTree.
	 */
	public HashTree(Collection keys) {
		data = new HashMap();
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			data.put(it.next(), new HashTree());
		}
	}

	/**
	 * Creates a new HashTree and adds all the objects in the given array as
	 * top-level nodes in the tree.
	 */
	public HashTree(Object[] keys) {
		data = new HashMap();
		for (int x = 0; x < keys.length; x++) {
			data.put(keys[x], new HashTree());
		}
	}

	/**
	 * If the HashTree contains the given object as a key at the top level, then
	 * a true result is returned, otherwise false.
	 * 
	 * @param o
	 *            Object to be tested as a key.
	 * @return True if the HashTree contains the key, false otherwise.
	 * @see java.util.Map#containsKey(Object)
	 */
	public boolean containsKey(Object o) {
		return data.containsKey(o);
	}

	/**
	 * If the HashTree is empty, true is returned, false otherwise.
	 * 
	 * @return True if HashTree is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/**
	 * Sets a key and it's value in the HashTree. It actually sets up a key, and
	 * then creates a node for the key and sets the value to the new node, as a
	 * key. Any previous nodes that existed under the given key are lost.
	 * 
	 * @param key
	 *            key to be set up
	 * @param value
	 *            value to be set up as a key in the secondary node
	 */
	public void set(Object key, Object value) {
		data.put(key, createNewTree(value));
	}

	/**
	 * Sets a key into the current tree and assigns it a HashTree as its
	 * subtree. Any previous entries under the given key are removed.
	 * 
	 * @param key
	 *            key to be set up
	 * @param t
	 *            HashTree that the key maps to
	 */
	public void set(Object key, HashTree t) {
		data.put(key, t);
	}

	/**
	 * Sets a key and its values in the HashTree. It sets up a key in the
	 * current node, and then creates a node for that key, and sets all the
	 * values in the array as keys in the new node. Any keys previously held
	 * under the given key are lost.
	 * 
	 * @param key
	 *            Key to be set up
	 * @param values
	 *            Array of objects to be added as keys in the secondary node
	 */
	public void set(Object key, Object[] values) {
		data.put(key, createNewTree(Arrays.asList(values)));
	}

	/**
	 * Sets a key and its values in the HashTree. It sets up a key in the
	 * current node, and then creates a node for that key, and set all the
	 * values in the array as keys in the new node. Any keys previously held
	 * under the given key are removed.
	 * 
	 * @param key
	 *            key to be set up
	 * @param values
	 *            Collection of objects to be added as keys in the secondary
	 *            node
	 */
	public void set(Object key, Collection values) {
		data.put(key, createNewTree(values));
	}

	/**
	 * Sets a series of keys into the HashTree. It sets up the first object in
	 * the key array as a key in the current node, recurses into the next
	 * HashTree node through that key and adds the second object in the array.
	 * Continues recursing in this manner until the end of the first array is
	 * reached, at which point all the values of the second array are set as
	 * keys to the bottom-most node. All previous keys of that bottom-most node
	 * are removed.
	 * 
	 * @param treePath
	 *            array of keys to put into HashTree
	 * @param values
	 *            array of values to be added as keys to bottom-most node
	 */
	public void set(Object[] treePath, Object[] values) {
		if (treePath != null && values != null) {
			set(Arrays.asList(treePath), Arrays.asList(values));
		}
	}

	/**
	 * Sets a series of keys into the HashTree. It sets up the first object in
	 * the key array as a key in the current node, recurses into the next
	 * HashTree node through that key and adds the second object in the array.
	 * Continues recursing in this manner until the end of the first array is
	 * reached, at which point all the values of the Collection of values are
	 * set as keys to the bottom-most node. Any keys previously held by the
	 * bottom-most node are lost.
	 * 
	 * @param treePath
	 *            array of keys to put into HashTree
	 * @param values
	 *            Collection of values to be added as keys to bottom-most node
	 */
	public void set(Object[] treePath, Collection values) {
		if (treePath != null) {
			set(Arrays.asList(treePath), values);
		}
	}

	/**
	 * Sets a series of keys into the HashTree. It sets up the first object in
	 * the key list as a key in the current node, recurses into the next
	 * HashTree node through that key and adds the second object in the list.
	 * Continues recursing in this manner until the end of the first list is
	 * reached, at which point all the values of the array of values are set as
	 * keys to the bottom-most node. Any previously existing keys of that bottom
	 * node are removed.
	 * 
	 * @param treePath
	 *            collection of keys to put into HashTree
	 * @param values
	 *            array of values to be added as keys to bottom-most node
	 */
	public void set(Collection treePath, Object[] values) {
		HashTree tree = addTreePath(treePath);
		tree.set(Arrays.asList(values));
	}

	/**
	 * Sets the nodes of the current tree to be the objects of the given
	 * collection. Any nodes previously in the tree are removed.
	 * 
	 * @param values
	 *            Collection of objects to set as nodes.
	 */
	public void set(Collection values) {
		clear();
		this.add(values);
	}

	/**
	 * Sets a series of keys into the HashTree. It sets up the first object in
	 * the key list as a key in the current node, recurses into the next
	 * HashTree node through that key and adds the second object in the list.
	 * Continues recursing in this manner until the end of the first list is
	 * reached, at which point all the values of the Collection of values are
	 * set as keys to the bottom-most node. Any previously existing keys of that
	 * bottom node are lost.
	 * 
	 * @param treePath
	 *            list of keys to put into HashTree
	 * @param values
	 *            collection of values to be added as keys to bottom-most node
	 */
	public void set(Collection treePath, Collection values) {
		HashTree tree = addTreePath(treePath);
		tree.set(values);
	}

	/**
	 * Adds an key into the HashTree at the current level.
	 * 
	 * @param key
	 *            key to be added to HashTree
	 */
	public HashTree add(Object key) {
		if (!data.containsKey(key)) {
			HashTree newTree = createNewTree();
			data.put(key, newTree);
			return newTree;
		}
		return getTree(key);
	}

	/**
	 * Adds all the given objects as nodes at the current level.
	 * 
	 * @param keys
	 *            Array of Keys to be added to HashTree.
	 */
	public void add(Object[] keys) {
		for (int x = 0; x < keys.length; x++) {
			add(keys[x]);
		}
	}

	/**
	 * Adds a bunch of keys into the HashTree at the current level.
	 * 
	 * @param keys
	 *            Collection of Keys to be added to HashTree.
	 */
	public void add(Collection keys) {
		Iterator it = keys.iterator();
		while (it.hasNext()) {
			add(it.next());
		}
	}

	/**
	 * Adds a key and it's value in the HashTree. The first argument becomes a
	 * node at the current level, and the second argument becomes a node of it.
	 * 
	 * @param key
	 *            key to be added
	 * @param value
	 *            value to be added as a key in the secondary node
	 */
	public HashTree add(Object key, Object value) {
		add(key);
		return getTree(key).add(value);
	}

	/**
	 * Adds a key and it's values in the HashTree. The first argument becomes a
	 * node at the current level, and adds all the values in the array to the
	 * new node.
	 * 
	 * @param key
	 *            key to be added
	 * @param values
	 *            array of objects to be added as keys in the secondary node
	 */
	public void add(Object key, Object[] values) {
		add(key);
		getTree(key).add(values);
	}

	/**
	 * Adds a key as a node at the current level and then adds all the objects
	 * in the second argument as nodes of the new node.
	 * 
	 * @param key
	 *            key to be added
	 * @param values
	 *            Collection of objects to be added as keys in the secondary
	 *            node
	 */
	public void add(Object key, Collection values) {
		add(key);
		getTree(key).add(values);
	}

	/**
	 * Adds a series of nodes into the HashTree using the given path. The first
	 * argument is an array that represents a path to a specific node in the
	 * tree. If the path doesn't already exist, it is created (the objects are
	 * added along the way). At the path, all the objects in the second argument
	 * are added as nodes.
	 * 
	 * @param treePath
	 *            an array of objects representing a path
	 * @param values
	 *            array of values to be added as keys to bottom-most node
	 */
	public void add(Object[] treePath, Object[] values) {
		if (treePath != null) {
			add(Arrays.asList(treePath), Arrays.asList(values));
		}
	}

	/**
	 * Adds a series of nodes into the HashTree using the given path. The first
	 * argument is an array that represents a path to a specific node in the
	 * tree. If the path doesn't already exist, it is created (the objects are
	 * added along the way). At the path, all the objects in the second argument
	 * are added as nodes.
	 * 
	 * @param treePath
	 *            an array of objects representing a path
	 * @param values
	 *            collection of values to be added as keys to bottom-most node
	 */
	public void add(Object[] treePath, Collection values) {
		if (treePath != null) {
			add(Arrays.asList(treePath), values);
		}
	}

	public HashTree add(Object[] treePath, Object value) {
		return add(Arrays.asList(treePath), value);
	}

	/**
	 * Adds a series of nodes into the HashTree using the given path. The first
	 * argument is a List that represents a path to a specific node in the tree.
	 * If the path doesn't already exist, it is created (the objects are added
	 * along the way). At the path, all the objects in the second argument are
	 * added as nodes.
	 * 
	 * @param treePath
	 *            a list of objects representing a path
	 * @param values
	 *            array of values to be added as keys to bottom-most node
	 */
	public void add(Collection treePath, Object[] values) {
		HashTree tree = addTreePath(treePath);
		tree.add(Arrays.asList(values));
	}

	/**
	 * Adds a series of nodes into the HashTree using the given path. The first
	 * argument is a List that represents a path to a specific node in the tree.
	 * If the path doesn't already exist, it is created (the objects are added
	 * along the way). At the path, the object in the second argument is added
	 * as a node.
	 * 
	 * @param treePath
	 *            a list of objects representing a path
	 * @param value
	 *            Object to add as a node to bottom-most node
	 */
	public HashTree add(Collection treePath, Object value) {
		HashTree tree = addTreePath(treePath);
		return tree.add(value);
	}

	/**
	 * Adds a series of nodes into the HashTree using the given path. The first
	 * argument is a SortedSet that represents a path to a specific node in the
	 * tree. If the path doesn't already exist, it is created (the objects are
	 * added along the way). At the path, all the objects in the second argument
	 * are added as nodes.
	 * 
	 * @param treePath
	 *            a SortedSet of objects representing a path
	 * @param values
	 *            Collection of values to be added as keys to bottom-most node
	 */
	public void add(Collection treePath, Collection values) {
		HashTree tree = addTreePath(treePath);
		tree.add(values);
	}

	protected HashTree addTreePath(Collection treePath) {
		HashTree tree = this;
		Iterator iter = treePath.iterator();
		while (iter.hasNext()) {
			Object temp = iter.next();
			tree.add(temp);
			tree = tree.getTree(temp);
		}
		return tree;
	}

	/**
	 * Gets the HashTree mapped to the given key.
	 * 
	 * @param key
	 *            Key used to find appropriate HashTree()
	 */
	public HashTree getTree(Object key) {
		return (HashTree) data.get(key);
	}

	/**
	 * Returns the HashTree object associated with the given key. Same as
	 * calling {@link #getTree(Object)}.
	 * 
	 * @see java.util.Map#get(Object)
	 */
	public Object get(Object key) {
		return getTree(key);
	}

	/**
	 * Gets the HashTree object mapped to the last key in the array by recursing
	 * through the HashTree structure one key at a time.
	 * 
	 * @param treePath
	 *            array of keys.
	 * @return HashTree at the end of the recursion.
	 */
	public HashTree getTree(Object[] treePath) {
		if (treePath != null) {
			return getTree(Arrays.asList(treePath));
		}
		return this;
	}

	/**
	 * Create a clone of this HashTree. This is not a deep clone (ie, the
	 * contents of the tree are not cloned).
	 * 
	 */
	public Object clone() {
		HashTree newTree = new HashTree();
		cloneTree(newTree);
		return newTree;
	}

	protected void cloneTree(HashTree newTree) {
		Iterator iter = list().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			newTree.set(key, (HashTree) getTree(key).clone());
		}
	}

	/**
	 * Creates a new tree. This method exists to allow inheriting classes to
	 * generate the appropriate types of nodes. For instance, when a node is
	 * added, it's value is a HashTree. Rather than directly calling the
	 * HashTree() constructor, the createNewTree() method is called. Inheriting
	 * classes should override these methods and create the appropriate subclass
	 * of HashTree.
	 * 
	 * @return HashTree
	 */
	protected HashTree createNewTree() {
		return new HashTree();
	}

	/**
	 * Creates a new tree. This method exists to allow inheriting classes to
	 * generate the appropriate types of nodes. For instance, when a node is
	 * added, it's value is a HashTree. Rather than directly calling the
	 * HashTree() constructor, the createNewTree() method is called. Inheriting
	 * classes should override these methods and create the appropriate subclass
	 * of HashTree.
	 * 
	 * @return HashTree
	 */
	protected HashTree createNewTree(Object key) {
		return new HashTree(key);
	}

	/**
	 * Creates a new tree. This method exists to allow inheriting classes to
	 * generate the appropriate types of nodes. For instance, when a node is
	 * added, it's value is a HashTree. Rather than directly calling the
	 * HashTree() constructor, the createNewTree() method is called. Inheriting
	 * classes should override these methods and create the appropriate subclass
	 * of HashTree.
	 * 
	 * @return HashTree
	 */
	protected HashTree createNewTree(Collection values) {
		return new HashTree(values);
	}

	/**
	 * Gets the HashTree object mapped to the last key in the SortedSet by
	 * recursing through the HashTree structure one key at a time.
	 * 
	 * @param treePath
	 *            Collection of keys
	 * @return HashTree at the end of the recursion
	 */
	public HashTree getTree(Collection treePath) {
		return getTreePath(treePath);
	}

	/**
	 * Gets a Collection of all keys in the current HashTree node. If the
	 * HashTree represented a file system, this would be like getting a
	 * collection of all the files in the current folder.
	 * 
	 * @return Set of all keys in this HashTree
	 */
	public Collection list() {
		return data.keySet();
	}

	/**
	 * Gets a Set of all keys in the HashTree mapped to the given key of the
	 * current HashTree object (in other words, one level down. If the HashTree
	 * represented a file system, this would like getting a list of all files in
	 * a sub-directory (of the current directory) specified by the key argument.
	 * 
	 * @param key
	 *            key used to find HashTree to get list of
	 * @return Set of all keys in found HashTree.
	 */
	public Collection list(Object key) {
		HashTree temp = (HashTree) data.get(key);
		if (temp != null) {
			return temp.list();
		}
		return new LinkedList();
	}

	/**
	 * Removes the entire branch specified by the given key.
	 * 
	 * @see java.util.Map#remove(Object)
	 */
	public Object remove(Object key) {
		return data.remove(key);
	}

	/**
	 * Recurses down into the HashTree stucture using each subsequent key in the
	 * array of keys, and returns the Set of keys of the HashTree object at the
	 * end of the recursion. If the HashTree represented a file system, this
	 * would be like getting a list of all the files in a directory specified by
	 * the treePath, relative from the current directory.
	 * 
	 * @param treePath
	 *            Array of keys used to recurse into HashTree structure
	 * @return Set of all keys found in end HashTree
	 */
	public Collection list(Object[] treePath) {
		if (treePath != null) {
			return list(Arrays.asList(treePath));
		}
		return list();
	}

	/**
	 * Recurses down into the HashTree stucture using each subsequent key in the
	 * List of keys, and returns the Set of keys of the HashTree object at the
	 * end of the recursion. If the HashTree represented a file system, this
	 * would be like getting a list of all the files in a directory specified by
	 * the treePath, relative from the current directory.
	 * 
	 * @param treePath
	 *            List of keys used to recurse into HashTree structure
	 * @return Set of all keys found in end HashTree
	 */
	public Collection list(Collection treePath) {
		HashTree tree = getTreePath(treePath);
		if (tree != null) {
			return tree.list();
		}
		return new LinkedList();
	}

	/**
	 * Finds the given current key, and replaces it with the given new key. Any
	 * tree structure found under the original key is moved to the new key.
	 */
	public void replace(Object currentKey, Object newKey) {
		HashTree tree = getTree(currentKey);
		data.remove(currentKey);
		data.put(newKey, tree);
	}

	/**
	 * Gets an array of all keys in the current HashTree node. If the HashTree
	 * represented a file system, this would be like getting an array of all the
	 * files in the current folder.
	 * 
	 * @return array of all keys in this HashTree.
	 */
	public Object[] getArray() {
		return data.keySet().toArray();
	}

	/**
	 * Gets an array of all keys in the HashTree mapped to the given key of the
	 * current HashTree object (in other words, one level down). If the HashTree
	 * represented a file system, this would like getting a list of all files in
	 * a sub-directory (of the current directory) specified by the key argument.
	 * 
	 * @param key
	 *            key used to find HashTree to get list of
	 * @return array of all keys in found HashTree
	 */
	public Object[] getArray(Object key) {
		HashTree t = getTree(key);
		if (t != null) {
			return t.getArray();
		}
		return null;
	}

	/**
	 * Recurses down into the HashTree stucture using each subsequent key in the
	 * array of keys, and returns an array of keys of the HashTree object at the
	 * end of the recursion. If the HashTree represented a file system, this
	 * would be like getting a list of all the files in a directory specified by
	 * the treePath, relative from the current directory.
	 * 
	 * @param treePath
	 *            array of keys used to recurse into HashTree structure
	 * @return array of all keys found in end HashTree
	 */
	public Object[] getArray(Object[] treePath) {
		if (treePath != null) {
			return getArray(Arrays.asList(treePath));
		}
		return getArray();
	}

	/**
	 * Recurses down into the HashTree stucture using each subsequent key in the
	 * treePath argument, and returns an array of keys of the HashTree object at
	 * the end of the recursion. If the HashTree represented a file system, this
	 * would be like getting a list of all the files in a directory specified by
	 * the treePath, relative from the current directory.
	 * 
	 * @param treePath
	 *            list of keys used to recurse into HashTree structure
	 * @return array of all keys found in end HashTree
	 */
	public Object[] getArray(Collection treePath) {
		HashTree tree = getTreePath(treePath);
		return (tree != null) ? tree.getArray() : null;
	}

	protected HashTree getTreePath(Collection treePath) {
		HashTree tree = this;
		Iterator iter = treePath.iterator();
		while (iter.hasNext()) {
			if (tree == null) {
				return null;
			}
			Object temp = iter.next();
			tree = tree.getTree(temp);
		}
		return tree;
	}

	/**
	 * Returns a hashcode for this HashTree.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return data.hashCode() * 7;
	}

	/**
	 * Compares all objects in the tree and verifies that the two trees contain
	 * the same objects at the same tree levels. Returns true if they do, false
	 * otherwise.
	 * 
	 * @param o
	 *            Object to be compared against
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof HashTree)) {
			return false;
		}
		HashTree oo = (HashTree) o;
		if (oo.size() != this.size()) {
			return false;
		}
		return data.equals(oo.data);

		// boolean flag = true;
		// if (o instanceof HashTree)
		// {
		// HashTree oo = (HashTree) o;
		// Iterator it = data.keySet().iterator();
		// while (it.hasNext())
		// {
		// if (!oo.containsKey(it.next()))
		// {
		// flag = false;
		// break;
		// }
		// }
		// if (flag)
		// {
		// it = data.keySet().iterator();
		// while (it.hasNext())
		// {
		// Object temp = it.next();
		// flag = get(temp).equals(oo.get(temp));
		// if (!flag)
		// {
		// break;
		// }
		// }
		// }
		// }
		// else
		// {
		// flag = false;
		// }
		// return flag;
	}

	/**
	 * Returns a Set of all the keys in the top-level of this HashTree.
	 * 
	 * @see java.util.Map#keySet()
	 */
	public Set keySet() {
		return data.keySet();
	}

	/**
	 * Searches the HashTree structure for the given key. If it finds the key,
	 * it returns the HashTree mapped to the key. If it finds nothing, it
	 * returns null.
	 * 
	 * @param key
	 *            Key to search for
	 * @return HashTree mapped to key, if found, otherwise <code>null</code>
	 */
	public HashTree search(Object key) {// TODO does not appear to be used
		HashTree result = getTree(key);
		if (result != null) {
			return result;
		}
		TreeSearcher searcher = new TreeSearcher(key);
		try {
			traverse(searcher);
		} catch (RuntimeException e) {
			if (!e.getMessage().equals(FOUND)) {
				throw e;
			}
			// do nothing - means object is found
		}
		return searcher.getResult();
	}

	/**
	 * Method readObject.
	 */
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Returns the number of top-level entries in the HashTree.
	 * 
	 * @see java.util.Map#size()
	 */
	public int size() {
		return data.size();
	}

	/**
	 * Allows any implementation of the HashTreeTraverser interface to easily
	 * traverse (depth-first) all the nodes of the HashTree. The Traverser
	 * implementation will be given notification of each node visited.
	 * 
	 * @see HashTreeTraverser
	 */
	public void traverse(HashTreeTraverser visitor) {
		Iterator iter = list().iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			visitor.addNode(item, getTree(item));
			getTree(item).traverseInto(visitor);
		}
	}

	/**
	 * The recursive method that accomplishes the tree-traversal and performs
	 * the callbacks to the HashTreeTraverser.
	 */
	private void traverseInto(HashTreeTraverser visitor) {

		if (list().size() == 0) {
			visitor.processPath();
		} else {
			Iterator iter = list().iterator();
			while (iter.hasNext()) {
				Object item = iter.next();
				final HashTree treeItem = getTree(item);
				visitor.addNode(item, treeItem);
				treeItem.traverseInto(visitor);
			}
		}
		visitor.subtractNode();
	}

	/**
	 * Generate a printable representation of the tree.
	 * 
	 * @return a representation of the tree
	 */
	public String toString() {
		ConvertToString converter = new ConvertToString();
		try {
			traverse(converter);
		} catch (Exception e) { // Just in case
			converter.reportError(e);
		}
		return converter.toString();
	}

	private static class TreeSearcher implements HashTreeTraverser {

		private final Object target;

		private HashTree result;

		public TreeSearcher(Object t) {
			target = t;
		}

		public HashTree getResult() {
			return result;
		}

		/** {@inheritDoc} */
		public void addNode(Object node, HashTree subTree) {
			result = subTree.getTree(target);
			if (result != null) {
				// short circuit traversal when found
				throw new RuntimeException(FOUND);
			}
		}

		/** {@inheritDoc} */
		public void processPath() {
			// Not used
		}

		/** {@inheritDoc} */
		public void subtractNode() {
			// Not used
		}
	}

	private static class ConvertToString implements HashTreeTraverser {
		private final StringBuffer string = new StringBuffer(getClass().getName() + "{");

		private final StringBuffer spaces = new StringBuffer();

		private int depth = 0;

		public void addNode(Object key, HashTree subTree) {
			depth++;
			string.append("\n").append(getSpaces()).append(key);
			string.append(" {");
		}

		public void subtractNode() {
			string.append("\n" + getSpaces() + "}");
			depth--;
		}

		public void processPath() {
		}

		public String toString() {
			string.append("\n}");
			return string.toString();
		}

		void reportError(Throwable t) {
			string.append("Error: ").append(t.toString());
		}

		private String getSpaces() {
			if (spaces.length() < depth * 2) {
				while (spaces.length() < depth * 2) {
					spaces.append("  ");
				}
			} else if (spaces.length() > depth * 2) {
				spaces.setLength(depth * 2);
			}
			return spaces.toString();
		}
	}
}