package com.example.freshmarket.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.freshmarket.R
import com.example.freshmarket.data.CartManager
import com.example.freshmarket.data.model.Product
import com.example.freshmarket.databinding.FragmentProductListBinding
import com.example.freshmarket.ui.GridSpacingItemDecoration
import com.example.freshmarket.ui.main.ProductAdapter

class ProductListFragment : Fragment() {

    // Используем backing property для binding
    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter

    // Список товаров
    private val products = listOf(
        Product(
            "1", "Помидоры", "Сочные и ароматные помидоры с насыщенным вкусом. Отлично подходят для салатов, соусов и горячих блюд. Богаты витаминами A и C, а также антиоксидантами, укрепляющими иммунитет. Выращены в экологически чистых условиях без химикатов.",
            110, "https://pngfre.com/wp-content/uploads/Tomato-17-2-1024x650.png"
        ),
        Product(
            "2", "Гранат", "Ароматный и сочный гранат с насыщенным рубиновым цветом и приятной кисло-сладкой ноткой. Содержит большое количество антиоксидантов, которые замедляют старение, укрепляют сосуды и повышают иммунитет. Гранат также богат железом, что делает его полезным для кроветворной системы. Можно употреблять в свежем виде, добавлять в салаты, соки и десерты. Яркий вкус и полезные свойства делают его одним из самых ценных фруктов для здоровья.",
            110, "https://pngimg.com/d/pomegranate_PNG8647.png"
        ),
        Product(
            "3", "Картофель", "Отборный картофель с плотной текстурой. Идеален для жарки, варки и запекания. Содержит крахмал и витамины, полезен в рационе.",
            60, "https://pngimg.com/d/potato_PNG7082.png"
        ),
        Product(
            "4", "Яблоки", "Сочные и сладкие яблоки с насыщенным вкусом и приятным ароматом. Отлично подходят для перекуса, выпечки, приготовления свежих соков и десертов. Благодаря высокому содержанию витаминов и антиоксидантов, укрепляют иммунитет и улучшают обмен веществ. Яблоки полезны для сердца, поддерживают уровень сахара в крови и богаты клетчаткой, которая способствует пищеварению.",
            80, "https://pngimg.com/d/apple_PNG12405.png"
        ),
        Product(
            "5", "<Бананы>", "Спелые бананы с мягкой текстурой и натуральной сладостью. Содержат большое количество калия, который укрепляет сердце, поддерживает уровень жидкости в организме и помогает избежать судорог. Бананы являются отличным источником энергии, их удобно брать с собой на перекус. Также богаты витаминами B6 и C, способствуют улучшению настроения и поддержанию здорового обмена веществ.",
            179, "https://pngimg.com/d/banana_PNG104276.png"
        ),
        Product(
            "6", "Клубника", "Сочные и ароматные ягоды с насыщенным сладко-кислым вкусом. Клубника богата витамином C, который укрепляет иммунитет и защищает кожу от старения. Благодаря высокому содержанию антиоксидантов, помогает организму бороться с воспалениями и свободными радикалами. Клубника используется в десертах, коктейлях и выпечке, а также является полезной основой для детского питания и диетического рациона.",
            370, "https://pngimg.com/d/strawberry_PNG2598.png"
        ),
        Product(
            "7", "Апельсин", "Сочные и сладкие апельсины с ярким цитрусовым ароматом. Они содержат большое количество витамина C, который помогает укрепить иммунитет, улучшает состояние кожи и защищает организм от простудных заболеваний. Апельсины также богаты клетчаткой, способствующей пищеварению, и антиоксидантами, замедляющими процессы старения. Используются в свежем виде, в соках, десертах и салатах.",
            120, "https://pngimg.com/d/orange_PNG791.png"
        ),
        Product(
            "8", "Виноград", "Сладкий и сочный виноград с насыщенным вкусом и высокой питательной ценностью. Содержит антиоксиданты, улучшающие состояние кожи, замедляющие старение и укрепляющие сердечно-сосудистую систему. Ягоды винограда богаты витаминами A, C и K, улучшают обмен веществ и придают энергию. Виноград можно есть в свежем виде, добавлять в десерты, салаты и использовать в соках и виноделии.",
            390, "https://freepngimg.com/save/13226-grape-png/1000x667"
        ),
        Product(
            "9", "Морковь", "Свежая и хрустящая морковь с насыщенным вкусом. Отличный источник бета-каротина, который превращается в витамин A и способствует здоровью глаз, кожи и иммунной системы. Морковь полезна для пищеварения, помогает очищать организм от токсинов и улучшает обмен веществ. Идеально подходит для свежих салатов, супов, гарниров, а также приготовления сока, который особенно полезен для кожи и зрения.",
            72, "https://static.vecteezy.com/system/resources/previews/030/663/871/large_2x/carrots-with-transparent-background-high-quality-ultra-hd-free-photo.jpg"
        ),
        Product(
            "10", "Кукуруза", "Сладкая и ароматная кукуруза, богатая клетчаткой и витаминами группы B. Помогает улучшить работу желудочно-кишечного тракта, поддерживает здоровье нервной системы и насыщает организм энергией. Может употребляться в свежем, вареном и консервированном виде. Идеально подходит как гарнир, а также как полезный перекус.",
            45, "https://parspng.com/wp-content/uploads/2022/09/cornpng.parspng.com_.png"
        ),
        Product(
            "11", "Авокадо", "Питательное и полезное авокадо с маслянистой текстурой и нежным ореховым вкусом. Богато полезными жирами, которые поддерживают здоровье сердца и улучшают состояние кожи. Также содержит витамины группы B, калий и магний, способствующие нормализации давления и работе нервной системы. Идеально подходит для салатов, смузи, тостов и приготовления гуакамоле. Благодаря высокой питательности, является отличным выбором для сбалансированного питания.",
            110, "https://freepngimg.com/save/9747-avocado-png-image/648x437"
        ),
        Product(
            "12", "Огурцы", "Свежие хрустящие огурцы с насыщенным вкусом. Отлично подходят для салатов, закусок и консервирования. Богаты водой и полезными микроэлементами.",
            90, "https://static.vecteezy.com/system/resources/previews/047/307/286/non_2x/fresh-cucumbers-with-leaves-and-flowers-free-png.png"
        ),
        Product(
            "13", "Ананас", "Сочный и сладкий ананас с тропическим ароматом. Богат витамином C, который укрепляет иммунитет, способствует улучшению пищеварения и защищает организм от воспалений. Ананас содержит бромелайн — фермент, помогающий расщеплять белки и ускорять обмен веществ. Отличный вариант для свежего употребления, приготовления десертов, соков и добавления в экзотические блюда. Благодаря освежающему вкусу, он идеально подходит для летних коктейлей и фруктовых салатов.",
            90, "https://img.pikbest.com/png-images/20241022/pineapple-png-isolated-on-transparent-background-high-resolution_10991161.png!sw800"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Инициализация RecyclerView и адаптера происходит в onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.item_inner_spacing)
        binding.rvProducts.addItemDecoration(GridSpacingItemDecoration(spacingPx))

        adapter = ProductAdapter(
            products,
            onProductClick = { product ->
                openProductDetails(product)
            },
            onAddToCartClick = { product ->
                CartManager.addToCart(product)
                Toast.makeText(
                    requireContext(),
                    "Добавлено в корзину: ${product.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        binding.rvProducts.adapter = adapter
    }

    private fun openProductDetails(product: Product) {
        val action = ProductListFragmentDirections.actionNavHomeToProductDetailsFragment(
            productId = product.id,
            productName = product.name,
            productDescription = product.description,
            productPrice = product.priceCents,
            productImageUrl = product.imageUrl
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
