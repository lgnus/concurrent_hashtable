**Trabalho de CP 2016**

Informações:

Considerámos três versões do repositório, isoladas nas seguintes três versões do ficheiro _Repository_:

* _RepositorySyncOperationGrained_ - Operações de inserção e remoção são bloqueantes pelo que a vantagem de concurrência é baixa (coarse grained)
* _RepositorySyncStructuresGrained_ - Operações de inserção e remoção bloqueam por completo o acesso às estruturas de dados (coarse grained)
* _Repository_ (**default**) - Operações sobre a tabela são concorrentes através de locks ao nivel dos buckets (middle grained), operações ao nivel do repositório apenas bloqueiam os buckets que pretendem usar. 

Alunos:

* José Carneiro nº41749
* Ruben Ramalho nº41846
