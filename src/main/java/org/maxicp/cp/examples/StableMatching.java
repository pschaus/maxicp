/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */

package org.maxicp.cp.examples;

import org.maxicp.cp.engine.constraints.Element1D;
import org.maxicp.cp.engine.constraints.Element1DVar;
import org.maxicp.cp.engine.core.BoolVar;
import org.maxicp.cp.engine.core.IntVar;
import org.maxicp.cp.engine.core.CPSolver;
import org.maxicp.search.DFSearch;
import org.maxicp.search.SearchStatistics;
import org.maxicp.util.io.InputReader;
import org.maxicp.BranchingScheme;
import org.maxicp.Factory;

import java.util.Arrays;

/**
 * Stable Matching problem:
 * Given n students and n companies, where each student (resp. company) has
 * ranked each company (resp. student) with a unique number between 1 and n
 * in order of preference (the lower the number, the higher the preference),
 * say for summer internships, match the students and companies such that
 * there is no pair of a student and a company who would both prefer to be
 * matched with each other than with their actually matched ones.
 * If there are no such pairs, then the matching is said to be stable.
 * <a href="https://en.wikipedia.org/wiki/Stable_matching_problem">Wikipedia</a>.
 */
public class StableMatching {

    public static void main(String[] args) {

        InputReader reader = new InputReader("data/stable_matching.txt");
        int n = reader.getInt();
        int[][] rankCompanies = reader.getMatrix(n, n);
        int[][] rankStudents = reader.getMatrix(n, n);

        // you should get six solutions:
        /*
        company: 5,3,8,7,2,6,0,4,1
        student: 6,8,4,1,7,0,5,3,2

        company: 5,4,8,7,2,6,0,3,1
        student: 6,8,4,7,1,0,5,3,2

        company: 5,0,3,7,4,8,2,1,6
        student: 1,7,6,2,4,0,8,3,5

        company: 5,0,3,7,4,6,2,1,8
        student: 1,7,6,2,4,0,5,3,8

        company: 5,3,0,7,4,6,2,1,8
        student: 2,7,6,1,4,0,5,3,8

        company: 6,4,8,7,2,5,0,3,1
        student: 6,8,4,7,1,5,0,3,2
        */

        CPSolver cp = Factory.makeSolver();

        // company[s] is the company chosen for student s
        IntVar[] company = Factory.makeIntVarArray(cp, n, n);
        // student[c] is the student chosen for company c
        IntVar[] student = Factory.makeIntVarArray(cp, n, n);

        // companyPref[s] is the preference of student s for the company chosen for s
        IntVar[] companyPref = Factory.makeIntVarArray(cp, n, n + 1);
        // studentPref[c] is the preference of company c for the student chosen for c
        IntVar[] studentPref = Factory.makeIntVarArray(cp, n, n + 1);


        for (int s = 0; s < n; s++) {
            // the student of the company of student s is s
            // TODO: model this with Element1DVar
            // STUDENT
            // BEGIN STRIP
            cp.post(new Element1DVar(student, company[s], Factory.makeIntVar(cp, s, s)));
            // END STRIP

            // TODO: model this with Element1D: rankCompanies[s][company[s]] = companyPref[s]
            // STUDENT
            // BEGIN STRIP
            cp.post(new Element1D(rankCompanies[s], company[s], companyPref[s]));
            // END STRIP

        }

        for (int c = 0; c < n; c++) {
            // the company of the student of company c is c
            // TODO: model this with Element1DVar
            // STUDENT
            // BEGIN STRIP
            cp.post(new Element1DVar(company, student[c], Factory.makeIntVar(cp, c, c)));
            // END STRIP

            // TODO: model this with Element1D: rankStudents[c][student[c]] = studentPref[c]
            // STUDENT
            // BEGIN STRIP
            cp.post(new Element1D(rankStudents[c], student[c], studentPref[c]));
            // END STRIP
        }

        for (int s = 0; s < n; s++) {
            for (int c = 0; c < n; c++) {
                // if student s prefers company c over the chosen company, then the opposite is not true: c prefers their chosen student over s
                // (companyPref[s] > rankCompanies[s][c]) => (studentPref[c] < rankStudents[c][s])

                BoolVar sPrefersC = Factory.isLarger(companyPref[s], rankCompanies[s][c]);
                BoolVar cDoesnot = Factory.isLess(studentPref[c], rankStudents[c][s]);
                cp.post(implies(sPrefersC, cDoesnot));

                // if company c prefers student s over their chosen student, then the opposite is not true: s prefers the chosen company over c
                // (studentPref[c] > rankStudents[c][s]) => (companyPref[s] < rankCompanies[s][c])
                // TODO: model this constraint
                // STUDENT
                // BEGIN STRIP
                BoolVar cPrefersS = Factory.isLarger(studentPref[c], rankStudents[c][s]);
                BoolVar sDoesnot = Factory.isLess(companyPref[s], rankCompanies[s][c]);
                cp.post(implies(cPrefersS, sDoesnot));
                // END STRIP

            }
        }


        DFSearch dfs = Factory.makeDfs(cp, BranchingScheme.and(BranchingScheme.firstFail(company), BranchingScheme.firstFail(student)));

        dfs.onSolution(() -> {
                    System.out.println(Arrays.toString(company));
                    System.out.println(Arrays.toString(student));
                }
        );


        SearchStatistics stats = dfs.solve();
        System.out.println(stats);

    }

    /**
     * Model the reified logical implication constraint
     * @param b1 left-hand side of the implication
     * @param b2 right-hand side of the implication
     * @return a boolean variable that is true if and only if
     *         the relation "b1 implies b2" is true, and false otherwise.
     */
    private static BoolVar implies(BoolVar b1, BoolVar b2) {
        IntVar notB1 = Factory.plus(Factory.minus(b1), 1);
        return Factory.isLargerOrEqual(Factory.sum(notB1, b2), 1);
    }
}

