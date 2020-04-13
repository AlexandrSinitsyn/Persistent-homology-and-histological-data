import matplotlib.pyplot as plt
import numpy as np
import pandas as pd


f = open('entropy.txt', 'r')

t = int(f.readline()) # 1
start = float(f.readline().split('\n')[0]) # 7.25
end = float(f.readline().split('\n')[0]) # 9.75
splitter = int(f.readline().split('\n')[0]) # 75
scale = int(f.readline().split('\n')[0]) # 600



entropies = {0: 'Entropy0', 1: 'Entropy1', 2: 'FullEntropy'}

TUM_ful = pd.read_csv('TumResults.csv', ';')
NORM = pd.read_csv('NormResults.csv', ';')

N = len(NORM)
TUM = TUM_ful.sample(n=N, random_state=1)


plt.figure(dpi=scale)
bins_E0 = np.linspace(start, end, splitter)

plt.xlabel('Энтропия')
plt.ylabel('N')
plt.gca().spines['top'].set_visible(False)
plt.gca().spines['right'].set_visible(False)

plt.hist(NORM[entropies[t]], bins_E0, alpha=0.5, label='здоровые')
plt.hist(TUM[entropies[t]], bins_E0, alpha=0.5, label='рак')
plt.legend(loc='upper left')

plt.savefig(f.readline().split('\n')[0])

f.close()

plt.show()
