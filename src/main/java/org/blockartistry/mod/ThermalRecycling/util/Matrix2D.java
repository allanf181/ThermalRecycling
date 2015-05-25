/* This file is part of ThermalRecycling, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.ThermalRecycling.util;

import com.google.common.base.Optional;

/**
 * Quick/easy 2D matrix implementation.  Backed by an array because
 * ArrayList<> doesn't behave in a sparse way.
 */
public final class Matrix2D<T> {

	protected final int rows;
	protected final int cols;
	protected final Object[] cells; 
	
	public Matrix2D(final int rows, final int cols) {
		this.rows = rows;
		this.cols = cols;
		this.cells = new Object[rows * cols];
	}
	
	public boolean isPresent(final int row, final int col) {
		return cells[row * cols + col] != null;
	}
	
	@SuppressWarnings("unchecked")
	public Optional<T> get(final int row, final int col) {
		return Optional.of((T)cells[row * cols + col]);
	}
	
	public void set(final int row, final int col, final T obj) {
		cells[row * cols + col] = obj;
	}
	
	public int getRowCount() {
		return rows;
	}
	
	public int getColCount() {
		return cols;
	}
}
