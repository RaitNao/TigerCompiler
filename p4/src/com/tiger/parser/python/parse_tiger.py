import copy
import itertools
import csv
import sys
import argparse
import os

DELIMITER = ""
EPSILON = ""
EPSILON_SET = set()
JAVA_CLASS_PATH = ""
TABLE_CSV_PATH = ""


"""
Function for longest prefix computation
"""
def allsame(x):
    return len(set(x)) == 1

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-e", "--epsilon", default='\xcf\xb5', type=str, dest='epsilon', help="String literal of Epsilon symbol")
    parser.add_argument("-d", "--delimiter", default='\xe2\x86\x92', type=str, dest='delimiter', help="String literal of production Delimiter")
    parser.add_argument("-j", "--java-class-path", default='../LLTable.java', type=str, dest='java_class_path', help="Path where to write the .java class with hardcoded LLTable")
    parser.add_argument("-t", "--table-csv-path", default='parse_table.csv', type=str, dest='table_csv_path', help="Path where to write the .csv with LL table")

    args = parser.parse_args()

    global EPSILON_SET, EPSILON, DELIMITER, JAVA_CLASS_PATH, TABLE_CSV_PATH
    EPSILON = args.epsilon
    EPSILON_SET = {EPSILON}
    DELIMITER = args.delimiter
    JAVA_CLASS_PATH = args.java_class_path
    TABLE_CSV_PATH = args.table_csv_path




    # Original Grammar
    G = {}
    # Ordering of original grammar
    text_order = []
    # Starting state
    S = ""


    # read terminals
    with open("terminals.txt", "r") as f:
        reader = csv.reader(f)
        terminals = reader.next()


    # parse grammar into Grammar dictionary
    with open("tiger.txt", "r") as f:
        last_alpha = ""

        for line in f:
           line = line.strip()

           production_ind = line.rfind(" ")
           production = line[:production_ind + 1]
           alpha, beta = production.split(DELIMITER)

           alpha = alpha.strip()
           beta = beta.strip()

           if S == "":
               S = alpha
           if alpha != last_alpha:
               text_order.append(alpha)
               last_alpha = alpha

           if G.get(alpha) == None:
               G[alpha] = [beta]
           else:
               G[alpha].append(beta)

    # Remove Left Factoring
    left_factored = True
    while left_factored:
        left_factored = False
        G_prime = copy.deepcopy(G)
        for NT in G:
            longest_common = []
            for i in range(len(G[NT])):
                for j in range(i + 1, len(G[NT])):
                    common = [x[0] for x in itertools.takewhile(allsame, itertools.izip(*[G[NT][i].split(), G[NT][j].split()]))]
                    if len(common) > len(longest_common):
                        longest_common = common

            if longest_common:
                str_longest_common = " ".join(longest_common)
                left_factored = True
                indexes = [ind for ind, l in enumerate(G[NT]) if len(l) >= len(str_longest_common) and l[:len(str_longest_common)] == str_longest_common]
                new_rules = [EPSILON if len(G[NT][ind]) == len(str_longest_common) else G[NT][ind][len(str_longest_common):].strip() for ind in indexes]
                new_NT = NT + "_lf"
                while G.get(new_NT):
                    new_NT += "_lf"

                G_prime[NT] = [rule for ind, rule in enumerate(G[NT]) if ind not in indexes] + [str_longest_common + " " + new_NT]
                G_prime[new_NT] = new_rules
        G = G_prime


    G_prime = copy.deepcopy(G)

    # Remove direct Left Recursion
    for NT in G:
        new_rules = []
        left_recursive = False
        NT_prime = NT + '\''
        for ind, rule in enumerate(G[NT]):
            if rule.find(NT) == 0 and rule[len(NT)] == ' ':
                left_recursive = True

                if G.get(NT_prime) != None:
                    G_prime[NT_prime].append(rule[len(NT):].lstrip() + NT_prime)
                else:
                    G_prime[NT_prime] = [rule[len(NT):].strip() + " " + NT_prime, EPSILON]

            else:
                new_rules.append(rule)

        if left_recursive:
            G_prime[NT] = [el + " " + NT_prime for el in new_rules]


    # Topological Sort. The ordering of Non-Terminals stored in @order
    order = []
    visited = set()
    for key in G_prime:
        if key not in visited:
            topsort(G_prime, key, visited, order)

    # Remove indirect Left Recursion
    G_final = copy.deepcopy(G_prime)
    for NT in order:
        changed = True
        while changed:
            changed = False
            for ind, rule in enumerate(G_prime[NT]):
                space_ind = rule.find(' ')
                beta = None
                indirect = None

                if space_ind != -1 and rule[:space_ind] in G_prime:
                    indirect = rule[:space_ind]
                elif rule in G_prime:
                    indirect = rule

                if indirect != None and -1 < order.index(indirect) < ind:
                    if space_ind != -1:
                        beta = rule[space_ind + 1:]
                    else:
                        beta = ""

                if beta != None:
                    G_final[NT].remove(rule)
                    new_rules = []
                    for subrule in G_final[indirect]:
                        new_rules.append(subrule + beta)

                    G_final[NT].extend(new_rules)

    # Add production number to productions
    # as a tuple (production derivation, production index)
    count = 1
    for NT in text_order:
        rule_pairs = []
        for rule in G_final[NT]:
            rule_pairs.append((rule, count))
            count += 1
        G_final[NT] = rule_pairs

    for NT in G_final:
        if NT[-1] == "'" or NT[-3:] == "_lf":
            rule_pairs = []
            for rule in G_final[NT]:
                rule_pairs.append((rule, count))
                count += 1
            G_final[NT] = rule_pairs


    with open("finaltiger.txt", "w") as f:
        for NT in text_order:
            for rule_pair in G_final[NT]:
                f.write(NT + " " + DELIMITER + " " + rule_pair[0] + " (" + str(rule_pair[1]) + ")")
                f.write("\n")
        for NT in G_final:
            if NT[-1] == "'" or NT[-3:] == "_lf":
                for rule_pair in G_final[NT]:
                    f.write(NT + " " + DELIMITER + " " + rule_pair[0] + " (" + str(rule_pair[1]) + ")")
                    f.write("\n")

    LL = LLGrammar(S, G_final, terminals)
    conflicts = LL.get_first_follow_conflicts()
    if conflicts:
        raise Exception("First/Follow conflict in Non-Terminals {}".format(*conflicts[0]))
    LL.export_to_csv(TABLE_CSV_PATH)
    LL.export_to_java_class(JAVA_CLASS_PATH, text_order)



def topsort(G, curr, visited, stack):
    visited.add(curr)
    for rule in G[curr]:
        space_ind = rule.find(' ')
        if space_ind != -1:
            if rule[:space_ind] in G and rule[:space_ind] not in visited:
                topsort(G, rule[:space_ind], visited, stack)
        else:
            if rule in G and rule not in visited:
                topsort(G, rule, visited, stack)

    stack.insert(0, curr)

class LLGrammar(object):
    def __init__(self, S, G, terminals):
        self.S = S
        self.G = G
        self.first = {}
        self.follow = {}
        self.first_plus = {}
        self.ll_table = {}
        self.terminals = terminals

        self._build_first()
        self._build_follow()
        self._build_first_plus()
        self._build_ll_table()


    def _build_first(self):

        for NT in self.G:
            self.first[NT] = set()

        for T in self.terminals:
            self.first[T] = {T}

        changed = True
        while changed:
            changed = False
            for NT in self.G:
                for rule_pair in self.G[NT]:
                    if rule_pair[0] == EPSILON:
                        if not EPSILON_SET.issubset(self.first[NT]):
                            changed = True
                        self.first[NT].update(EPSILON_SET)
                        continue

                    derivation = rule_pair[0].split()
                    if not set(self.first[derivation[0]] - EPSILON_SET).issubset(self.first[NT]):
                        changed = True

                    self.first[NT].update(self.first[derivation[0]] - EPSILON_SET)

                    ind = 0
                    while ind < len(derivation) - 1 and EPSILON in self.first[derivation[ind]]:
                        if not set(self.first[derivation[ind]] - EPSILON_SET).issubset(self.first[NT]):
                            changed = True

                        self.first[NT].update(self.first[derivation[ind + 1]] - EPSILON_SET)
                        ind += 1

                    if ind == len(derivation) - 1 and EPSILON in self.first[derivation[ind]]:
                        if not EPSILON_SET.issubset(self.first[NT]):
                            changed = True
                        self.first[NT].update(EPSILON_SET)
        self.first["typedecl"] = {"type"}


    def _build_follow(self):
        for NT in self.G:
            self.follow[NT] = set()
        self.follow[self.S] = {"eof"}

        changed = True
        while changed:
            changed = False
            for NT in self.G:
                for rule_pair in self.G[NT]:
                    if rule_pair[0] == EPSILON:
                        continue
                    derivation = rule_pair[0].split()
                    trailer = self.follow[NT].copy()
                    for beta in reversed(derivation):
                        if beta in self.G:
                            if not trailer.issubset(self.follow[beta]):
                                changed = True
                            self.follow[beta].update(trailer)
                            if EPSILON in self.first[beta]:
                                trailer.update(self.first[beta] - EPSILON_SET)
                            else:
                                trailer = self.first[beta].copy()
                        else:
                            trailer = self.first[beta].copy()

    def _build_first_plus(self):
        for NT in self.G:
            self.first_plus[NT] = []
        for NT in self.G:
            for rule_pair in self.G[NT]:
                if rule_pair[0] == EPSILON:
                    self.first_plus[NT].append( (rule_pair[1], set(EPSILON_SET | self.follow[NT])) )
                else:
                    derivation = rule_pair[0].split()
                    if EPSILON in self.first[derivation[0]]:
                        self.first_plus[NT].append( (rule_pair[1], set(self.first[derivation[0]]) | set(self.follow[derivation[0]])) )
                    else:
                        self.first_plus[NT].append( (rule_pair[1], set(self.first[derivation[0]])) )



    def _build_ll_table(self):
        for NT in self.G:
            self.ll_table[NT] = {T: "E" for T in self.terminals}
        for NT in self.G:
            for term_pair in self.first_plus[NT]:
                for T in term_pair[1]:
                    if T in self.terminals:
                        if self.ll_table[NT][T] != "E":
                            raise Exception("More than 1 production per entry in Non-Terminal {} on Terminal {}: productions ({}) ({})".format(NT, T, self.ll_table[NT][T], term_pair[0]))
                        self.ll_table[NT][T] = term_pair[0]

    def get_first_follow_conflicts(self):
        conflicts = []

        for NT in self.G:
            if EPSILON in self.first[NT] and self.first[NT].intersection(self.follow[NT]):
                conflicts.append( (NT, self.first[NT].intersection(self.follow[NT]), self.first[NT], self.follow[NT]) )
        return conflicts

    def export_to_csv(self, path):
        if path.rfind('/') != -1:
            directory = path[:path.rfind('/')]
            if not os.path.isdir(directory):
                print "DOESNT EXIST"
                os.makedirs(directory)

        with open(path, "w") as csvfile:
            csvwriter = csv.writer(csvfile)
            csvwriter.writerow(['Non-Terminals'] + self.terminals)

            rows = []
            for NT in self.ll_table:
                row = [NT]
                for T in self.terminals:
                    row.append(self.ll_table[NT][T])
                rows.append(row)

            csvwriter.writerows(rows)

    def export_to_java_class(self, path, text_order):
        with open("terminals_to_enum.csv", 'r') as f:
            csvreader = csv.reader(f)
            translation = {el[0]: el[1] for el in csvreader}


        productions = []
        for NT in text_order:
            NT_name = actual_name(NT)

            for rule_pair in self.G[NT]:
                production = "new TigerProduction({}, ".format(NT_name)
                production += ", ".join([actual_name(el) if el in self.G and not (NT == "typedecl" and ind == 0) else "new TigerToken(TokenType.{})".format(translation[el]) for ind, el in enumerate(rule_pair[0].split())])
                production += ")"
                productions.append(production)

        for NT in self.G:
            if NT[-1] == "'" or NT[-3:] == "_lf":
                NT_name = actual_name(NT)

                for rule_pair in self.G[NT]:
                    production = "new TigerProduction({}, ".format(NT_name)
                    production += ", ".join([actual_name(el) if el in self.G else "new TigerToken(TokenType.{})".format(translation[el]) for el in rule_pair[0].split()])
                    production += ")"
                    productions.append(production)



        if path.rfind('/') != -1:
            directory = path[:path.rfind('/')]
            if not os.path.isdir(directory):
                print "DOESNT EXIST"
                os.makedirs(directory)


        code = [
"""package com.tiger.parser;

import java.util.Map;
import java.util.HashMap;
import com.tiger.scanner.TokenType;
import com.tiger.scanner.TigerToken;

public class LLTable {{

    private static final TigerProduction[] productions = {{
        {}
        }};

    private static final Map<TigerNT, Map<TokenType, TigerProduction>> map = createMap();

    private static Map<TigerNT, Map<TokenType, TigerProduction>> createMap() {{
        Map<TigerNT, Map<TokenType, TigerProduction>> map = new HashMap<>();""".format(",\n        ".join(productions))]

        for NT in self.ll_table:
            NT_name = actual_name(NT)
            map_entry = ["""\n        map.put({}, new HashMap<TokenType, TigerProduction>() {{\n            {{""".format(NT_name)]

            for term_pair in self.first_plus[NT]:
                for T in term_pair[1]:
                    if T in self.terminals:
                        pair = """\n                put(TokenType.{}, productions[{}]);""".format(translation[T], term_pair[0] - 1)
                        map_entry.append(pair)

            map_entry.append("""\n            }\n        });""")
            code.extend(map_entry)

        code.append("""\n        return map;\n    }\n""")

        code.append("""
    public static final TigerNT startSymbol = new TigerNT("program");
    public static final TigerToken EOF = new TigerToken(TokenType.EOF);

    public static TigerProduction getProduction(TigerNT NT, TigerToken token) {
        Map<TokenType, TigerProduction> byNT = map.get(NT);
        if (byNT == null) {
			return null;
		}

		return byNT.get(token.getType());
	}\n}\n""")

        with open(path, 'w') as f:
            f.writelines(code)

def actual_name(NT):
    if NT[-1] == "'":
        return "new TigerNT(" + '"' + NT[:-1] + '"' + ", NTType.LR)"
    elif NT[-3:] == "_lf":
        return "new TigerNT(" + '"' + NT[:-3] + '"' + ", NTType.LF)"
    else:
        return "new TigerNT(" + '"' + NT + '")'


if __name__ == '__main__':
    main()

